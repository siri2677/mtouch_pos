package com.example.data.service

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.Service.USB_SERVICE
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.service.BluetoothCardReaderService.Action1Receiver
import com.example.data.service.BluetoothCardReaderService.Action2Receiver
import com.example.domain.model.cardreader.CardReaderStatus
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import java.io.IOException

class UsbCardReaderService(): Service(), SerialInputOutputManager.Listener {
    companion object {
        private const val NOTIFICATION_ID = 2
        private const val CHANNEL_ID = "MyServiceChannel"
    }

    private val usbManager by lazy { getSystemService(USB_SERVICE) as UsbManager }
    private val notificationManager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }
    private lateinit var usbIoManager: SerialInputOutputManager
    private lateinit var usbSerialPort: UsbSerialPort
    private lateinit var usbDevice: UsbDevice
    private lateinit var connectReaderJob: Job
    private var retryCount = 0

    inner class UsbServiceBinder : Binder() {
        fun getService(): UsbCardReaderService = this@UsbCardReaderService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return UsbServiceBinder()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "My Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for My Service"
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val action1Intent = Intent(this, Action1Receiver::class.java)
        action1Intent.setAction("ACTION1")
        val action1PendingIntent = PendingIntent.getBroadcast(this, 0, action1Intent, PendingIntent.FLAG_IMMUTABLE)

        val action2Intent = Intent(this, Action2Receiver::class.java)
        action2Intent.setAction("ACTION2")
        val action2PendingIntent = PendingIntent.getBroadcast(this, 0, action2Intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationCompat = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("mtouch앱 실행중입니다.")
            .setContentText("USB 연결 진행 상태입니다.")
            .setSmallIcon(R.drawable.sym_def_app_icon)
            .addAction(R.drawable.sym_def_app_icon, "Action 1", action1PendingIntent)
            .addAction(R.drawable.sym_def_app_icon, "Action 2", action2PendingIntent)
            .build()
        startForeground(NOTIFICATION_ID, notificationCompat)
    }

    fun connect(deviceInfo: String) {
        this.usbDevice = getUsbDevice(deviceInfo)
        try {
            if (isConnected()) {
                createNotificationChannel()
                CardReaderResponse.connectionStatus.emitWithInCoroutine(CardReaderStatus.Connection.Active)
            } else {
                connectRetry()
            }
        } catch (e: NoSuchElementException) {
            connectRetry()
        }
    }

    private fun connectRetry() {
        if(::connectReaderJob.isInitialized) connectReaderJob.cancel()
        connectReaderJob = flow<Unit> {
            CardReaderResponse.connectionStatus.emit(CardReaderStatus.Connection.Establishing(retryCount++))
            connectProcess(usbDevice)
            delay(1500)
            if(isConnected()) {
                createNotificationChannel()
                CardReaderResponse.connectionStatus.emit(CardReaderStatus.Connection.Active)
                stopRetry()
            }
        }.retryWhen { cause, attempt ->
            delay(1500)
            cause.handleRetryException()
        }.catch {
        }.onCompletion {
            if (it == null) stopRetry()
        }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    private fun connectProcess(usbDevice: UsbDevice) {
        val driver = UsbSerialProber.getDefaultProber().probeDevice(usbDevice)
        usbSerialPort = driver.ports[0].also {
            it.open(usbManager.openDevice(driver.device))
            it.setParameters(38400, 8, 1, UsbSerialPort.PARITY_NONE)
        }
        usbIoManager = SerialInputOutputManager(usbSerialPort, this).also {
            it.start()
        }
    }

    fun stopRetry() {
        retryCount = 0
        if(::connectReaderJob.isInitialized) connectReaderJob.cancel()
    }

    private fun Throwable.handleRetryException() = when (this) {
        is IOException, is NullPointerException, is NoSuchElementException -> true
        is IllegalArgumentException -> {
            try {
                usbManager.hasPermission(usbDevice)
            } catch (e: Exception) {
                true
            }
        }
        else -> false
    }

    fun disConnect() {
        try {
            stopRetry()
            stopForeground(STOP_FOREGROUND_REMOVE)
            usbIoManager.listener = null
            usbIoManager.stop()
            usbSerialPort.close()
            CardReaderResponse.connectionStatus.emitWithInCoroutine(CardReaderStatus.Connection.Inactive)
        } catch (e: Exception){
        }
    }

    fun sendData(byteArray: ByteArray) {
        val spn = StringBuilder()
            .append("request bytes: ")
            .append(byteArray.toHex())
            .append("\n")
        Log.w("requestData", spn.toString())
        if(isConnected()) { usbSerialPort.write(byteArray, 0) }
    }

    override fun onNewData(data: ByteArray) {
        val spn = StringBuilder()
            .append("request bytes: ")
            .append(data.toHex())
            .append("\n")
        Log.w("responseData", spn.toString())
        CardReaderResponse.onResultCommunicate(data)
    }

    override fun onRunError(e: Exception?) {
        if (e?.message == "USB get_status request failed") { connectRetry() }
    }

    private fun isConnected() = if(::usbIoManager.isInitialized && ::usbSerialPort.isInitialized) {
        usbIoManager.state == SerialInputOutputManager.State.RUNNING
    } else false

    private fun getUsbDevice(usbDeviceInformation: String): UsbDevice {
        fun infoFormat(info: String) = info.replace(Regex("""/dev/bus/usb/\d+/\d+="""), "")
            .replace(Regex("""mSerialNumberReader=[^,]+,"""), "")
            .replace(Regex("""mName=[^,]+,"""), "")
            .replace(Regex(""" mHasAudioPlayback=[^,]+, """), "")
            .replace(Regex("""mHasAudioCapture=[^,]+, """), "")
            .replace(Regex("""mHasMidi=[^,]+, """), "")
            .replace(Regex("""mHasVideoCapture=[^,]+, """), "")
            .replace(Regex("""mHasVideoPlayback=[^,]+, """), "")

        return usbManager.deviceList.values.firstOrNull {
            infoFormat(it.toString()) == infoFormat(usbDeviceInformation)
        } ?: throw NoSuchElementException()
    }

    private fun ByteArray.toHex(): String = joinToString(" ") { "%02X".format(it) }

    private fun <T> MutableSharedFlow<T>.emitWithInCoroutine(cardReaderStatus: T) {
        CoroutineScope(Dispatchers.IO).launch { this@emitWithInCoroutine.emit(cardReaderStatus) }
    }
}