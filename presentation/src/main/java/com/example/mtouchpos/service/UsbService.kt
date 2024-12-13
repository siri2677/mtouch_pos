package com.example.mtouchpos.service

import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mtouchpos.managerImpl.cardreader.CardReaderResponseImpl
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.retryWhen
import java.io.IOException

@AndroidEntryPoint
class UsbService: Service(), SerialInputOutputManager.Listener {
    companion object {
        const val USB_DEVICE = "usbDevice"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "MyServiceChannel"
        private const val ACTION_GRANT_USB = "ACTION_GRANT_USB"
    }

    private val usbManager by lazy { getSystemService(USB_SERVICE) as UsbManager }
    private lateinit var usbIoManager: SerialInputOutputManager
    private lateinit var usbSerialPort: UsbSerialPort
    private lateinit var usbDevice: String
    private var connectUsbJob: Job? = null
    private var retryCount = 0

    private inner class UsbPermissionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_GRANT_USB) {
                connectWithPermissionCheck(usbDevice)
                unregisterReceiver(this)
            }
        }
    }

    inner class MyBinder : Binder() { fun getService() = this@UsbService }

    override fun onBind(intent: Intent?) = MyBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        intent?.getStringExtra(USB_DEVICE)?.let { connectWithPermissionCheck(it) }
        return START_STICKY
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
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(notificationChannel)
        }

        val notificationCompat = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("mtouch앱 실행중입니다.")
            .setContentText("USB 연결 진행 상태입니다.")
            .setSmallIcon(R.drawable.sym_def_app_icon)
            .build()
        startForeground(NOTIFICATION_ID, notificationCompat)
    }

    private fun connectProcess(usbDevice: UsbDevice) {
        val driver = UsbSerialProber.getDefaultProber().probeDevice(usbDevice)
        usbSerialPort = driver.ports[0].also {
            it.open(usbManager.openDevice(driver.device))
            it.setParameters(38400, 8, 1, UsbSerialPort.PARITY_NONE)
        }
        usbIoManager = SerialInputOutputManager(usbSerialPort, this@UsbService).also {
            it.start()
        }
    }


    fun connectWithPermissionCheck(usbDevice: String) {
        try {
            val device = getUsbDevice(usbDevice)
            if(usbManager.hasPermission(device)){
                CardReaderResponseImpl.onRegistered()
            } else {
                requestPermission(device)
            }
        } catch (e: NoSuchElementException) {
            CardReaderResponseImpl.onError("장치와의 연결이 끊긴 상태입니다.")
        }
    }

    fun connect(usbDevice: String) {
        this.usbDevice = usbDevice
        try {
            val device = getUsbDevice(usbDevice)
            if (!usbManager.hasPermission(device)) {
                CardReaderResponseImpl.onError("장치권한이 해제된 상태입니다.")
                return
            }
            if (isConnected()) {
                CardReaderResponseImpl.onConnected()
            } else {
                connectWithRetry()
            }
        } catch (e: NoSuchElementException) {
            connectWithRetry()
        }
    }

    fun stopRetry(byteArray: ByteArray?) {
        connectUsbJob?.cancel()
        retryCount = 0
        byteArray?.let { sendData(it) }
    }

    private fun connectWithRetry() {
        connectUsbJob?.cancel()
        connectUsbJob = flow<Unit> {
            CardReaderResponseImpl.onDisConnected(retryCount++)
            connectProcess(getUsbDevice(usbDevice))
            delay(1500)
            if(isConnected()) {
                CardReaderResponseImpl.onConnected()
                stopRetry(null)
            }
        }.retryWhen { cause, attempt ->
            delay(1500)
            cause.handleRetryException()
        }.catch {
        }.onCompletion {
            if (it == null) retryCount = 0
        }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    private fun Throwable.handleRetryException() = when (this) {
        is IOException, is NullPointerException, is NoSuchElementException -> true
        is IllegalArgumentException -> {
            try {
                usbManager.hasPermission(getUsbDevice(usbDevice))
            } catch (e: Exception) {
                true
            }
        }
        else -> false
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun requestPermission(usbDevice: UsbDevice) {
        registerReceiver(UsbPermissionReceiver(), IntentFilter(ACTION_GRANT_USB))
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_GRANT_USB), flags)
        usbManager.requestPermission(usbDevice, permissionIntent)
    }

    fun isConnected() = if(::usbIoManager.isInitialized && ::usbSerialPort.isInitialized) {
        usbIoManager.state == SerialInputOutputManager.State.RUNNING
    } else false

    fun disConnect() {
        try {
            usbIoManager.listener = null
            usbIoManager.stop()
            usbSerialPort.close()
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
        CardReaderResponseImpl.onResultCommunicate(data)
    }

    override fun onRunError(e: Exception?) {
        if (e?.message == "USB get_status request failed") { connectWithRetry() }
    }

    private fun getUsbDevice(usbDeviceInformation: String): UsbDevice {
        fun stringFormat(string: String) = string.replace(Regex("""/dev/bus/usb/\d+/\d+="""), "")
            .replace(Regex("""mSerialNumberReader=[^,]+,"""), "")
            .replace(Regex("""mName=[^,]+,"""), "")
            .replace(Regex(""" mHasAudioPlayback=[^,]+, """), "")
            .replace(Regex("""mHasAudioCapture=[^,]+, """), "")
            .replace(Regex("""mHasMidi=[^,]+, """), "")
            .replace(Regex("""mHasVideoCapture=[^,]+, """), "")
            .replace(Regex("""mHasVideoPlayback=[^,]+, """), "")

        return usbManager.deviceList.values.firstOrNull { stringFormat(it.toString()) == stringFormat(usbDeviceInformation) }
            ?: throw NoSuchElementException()
    }

    private fun ByteArray.toHex(): String = joinToString(" ") { "%02X".format(it) }

    override fun onDestroy() {
        super.onDestroy()
        try {
            disConnect()
        } catch (e: Exception){
        }
        CardReaderResponseImpl.onDisConnected(0)
    }
}