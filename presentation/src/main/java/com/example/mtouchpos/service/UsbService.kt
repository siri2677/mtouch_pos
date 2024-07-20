package com.example.mtouchpos.service

import android.R
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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.mtouchpos.viewmodel.usecasemanager.reader.DeviceCommunicateResponseDataImpl
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UsbService: Service(), SerialInputOutputManager.Listener {
    companion object {
        const val USB_DEVICE = "usbDevice"
    }

    private val notificationId = 1
    private val channelId = "MyServiceChannel"
    private val actionGrantUsb = "ACTION_GRANT_USB"

    private lateinit var usbIoManager: SerialInputOutputManager
    lateinit var usbSerialPort: UsbSerialPort

    private inner class UsbPermissionReceiver : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == actionGrantUsb) {
                val usbManager = context.getSystemService(USB_SERVICE) as UsbManager
                val usbDevice = intent.getStringExtra("usbDevice")
                if(usbManager.hasPermission(getUsbDevice(usbDevice!!))){
                    connectProcess(usbDevice!!)
                }
                context.unregisterReceiver(this)
            }
        }
    }

    inner class MyBinder : Binder() {
        fun getService(): UsbService = this@UsbService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return MyBinder()
    }

    override fun onCreate() {
        super.onCreate()
        if (Intent().action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
            Log.w("disConnect", "usb")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val usbDevice = intent?.getStringExtra(USB_DEVICE)!!
        val usbManager = this.getSystemService(USB_SERVICE) as UsbManager
        if(usbManager.hasPermission(getUsbDevice(usbDevice))){
            connectProcess(usbDevice)
        } else {
            this.registerReceiver(UsbPermissionReceiver(), IntentFilter(actionGrantUsb))
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            val intent = Intent(actionGrantUsb)
            intent.putExtra("usbDevice", usbDevice)
            val permissionIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags)
            usbManager.requestPermission(getUsbDevice(usbDevice), permissionIntent)
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                NotificationChannel(channelId, "My Service Channel", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Channel for My Service"
                }
            )
        }
        startForeground(
            notificationId,
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("mtouch앱 실행중입니다.")
                .setContentText("USB 연결 진행 상태입니다.")
                .setSmallIcon(R.drawable.sym_def_app_icon)
                .build()
        )
    }

    private fun connectProcess(usbDevice: String) {
        var driver = UsbSerialProber.getDefaultProber().probeDevice(getUsbDevice(usbDevice))
        try {
            val usbManager = applicationContext.getSystemService(USB_SERVICE) as UsbManager
            val usbConnection = usbManager.openDevice(driver.device)
            driver.ports[0].also {
                it.open(usbConnection)
                it.setParameters(38400, 8, 1, UsbSerialPort.PARITY_NONE)
            }
            usbIoManager = SerialInputOutputManager(usbSerialPort, this@UsbService).also {
                it.start()
            }
            DeviceCommunicateResponseDataImpl.onConnected()
        } catch (e: Exception) {
            Log.w("error", "disconnetDevice")
        }
    }
    fun isUsbGattInitialized() = ::usbSerialPort.isInitialized

    fun disConnect() {
        try {
            usbIoManager.listener = null
            usbIoManager.stop()
            usbSerialPort.close()
        } catch (e: Exception){
        }
    }

    override fun onNewData(data: ByteArray) {
        DeviceCommunicateResponseDataImpl.onResultCommunicate(data)
    }

    override fun onRunError(e: Exception?) {
        val mainLooper = Handler(Looper.getMainLooper())
        mainLooper.post {
            if(e?.message == "USB get_status request failed"){
                stopSelf()
            }
        }
    }

    private fun getUsbDevice(usbDeviceInformation: String): UsbDevice? {
        (applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager).deviceList.values.map {
            if(stringFormat(it.toString()) == stringFormat(usbDeviceInformation)) { return it }
        }
        return null
    }

    private fun stringFormat(string: String): String {
        val regex = Regex("""mSerialNumberReader=[^,]+""")
        return regex.replace(string, "")
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            disConnect()
        } catch (e: Exception){
        }
        DeviceCommunicateResponseDataImpl.onDisConnected()
    }
}