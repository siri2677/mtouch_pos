package com.example.domain.service

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.domain.usecase.DeviceSettingSharedPreferenceImpl
import com.example.domain.usecase.usb.UsbDeviceConnectUsecase
import com.example.domain.usecase.usb.UsbDeviceSearchUseCase
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import io.reactivex.rxjava3.subjects.PublishSubject
import java.lang.Exception

class UsbConnectService : Service(), SerialInputOutputManager.Listener {
    companion object {
        private const val NOTIFICATION_ID = 2
        private const val CHANNEL_ID = "usbServiceChannel"
    }
    private var actionGrantUsb = "ACTION_GRANT_USB"

    var usbSerialPort: UsbSerialPort? = null
    var usbIoManager: SerialInputOutputManager? = null

    private val binder = MyBinder()
    val dataSubject: PublishSubject<ByteArray> = PublishSubject.create()

    val permissionCheckComplete: PublishSubject<Boolean> = PublishSubject.create()

    inner class MyBinder : Binder() {
        fun getService(): UsbConnectService = this@UsbConnectService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        connectDevice()
//        if (isDevicePermissionGrant()) { connectDevice() } else { requestPermission() }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My Service Channel"
            val descriptionText = "Channel for My Service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("mtouch앱 실행중입니다.")
            .setContentText("usb 연결 진행 상태입니다.")
            .setSmallIcon(R.drawable.sym_def_app_icon)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getUsbDevice(): UsbDevice? {
        return UsbDeviceSearchUseCase(this).getUsbDevice()
    }

    private fun connectDevice() {
        val usbManager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        var driver: UsbSerialDriver = UsbSerialProber.getDefaultProber().probeDevice(getUsbDevice())
        if (usbManager.hasPermission(driver.device)) {
            try {
                Log.w("connecting", "BluetoothDevice")
                permissionCheckComplete.onNext(true)
                val usbConnection = usbManager.openDevice(driver.device)
                usbSerialPort = driver.ports[0]
                usbSerialPort.also {
                    it?.open(usbConnection)
                    it?.setParameters(38400, 8, 1, UsbSerialPort.PARITY_NONE)
                }
                usbIoManager = SerialInputOutputManager(usbSerialPort, this@UsbConnectService)
                usbIoManager?.start()

            } catch (e: Exception) {
                Log.w("error", "disconnetDevice")
            }
        } else {
            Log.w("permissioncheck", "permissioncheck")
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            var intent = Intent(actionGrantUsb)
            intent.putExtra("usbDevice", getUsbDevice())
            val permissionIntent: PendingIntent =
                PendingIntent.getBroadcast(this, 0, intent, flags)
            usbManager.requestPermission(getUsbDevice(), permissionIntent)
        }
    }

    fun requestSerialCommunication(byteArray: ByteArray?) {
        usbSerialPort?.write(byteArray, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (usbIoManager != null) {
            usbIoManager.also {
                it?.listener = null
                it?.stop()
            }
        }
        if (usbSerialPort != null) {
            usbSerialPort?.close()
        }
        if(!DeviceSettingSharedPreferenceImpl(this).isUsbDisConnectButtonClick()) {
//            UsbDeviceConnectUsecase(this).usbDeviceConnect()
        } else {
            DeviceSettingSharedPreferenceImpl(this).setUsbDisConnectButtonClick(false)
//            DeviceSettingSharedPreferenceImpl(this).clearUsbDeviceInformation()
        }
        this.stopSelf()
    }

    override fun onNewData(data: ByteArray?) {
        Log.w("UsbConnectService onNew", data!!.size.toString())
        data?.let { dataSubject.onNext(it) }
    }

    override fun onRunError(e: Exception?) {
        val mainLooper = Handler(Looper.getMainLooper())
        mainLooper.post {
            Log.w("disconnectDevice", "disconnectDevice")
        }
    }
}