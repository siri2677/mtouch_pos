package com.example.mtouchpos.device.usb

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
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.mtouchpos.FlowManager
import com.example.mtouchpos.vo.DeviceContentsVO
import com.example.mtouchpos.vo.DeviceConnectSharedFlow
import com.example.mtouchpos.vo.DeviceSerialCommunicate
import com.example.domain.vo.DeviceType
import com.example.mtouchpos.device.ResponseSerialCommunicationFormat
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UsbDeviceConnectService: Service(), SerialInputOutputManager.Listener {
    private val notificationId = 1
    private val channelId = "MyServiceChannel"
    private val actionGrantUsb = "ACTION_GRANT_USB"
    private lateinit var usbSerialPort: UsbSerialPort
    private lateinit var usbIoManager: SerialInputOutputManager

    inner class MyBinder : Binder() {
        fun getService(): UsbDeviceConnectService = this@UsbDeviceConnectService
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
        Log.w("onStartCommandusb", "onStartCommandusb")
        val usbDevice = intent?.getStringExtra(DeviceType.Usb.name)!!
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
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My Service Channel"
            val descriptionText = "Channel for My Service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("mtouch앱 실행중입니다.")
            .setContentText("USB 연결 진행 상태입니다.")
            .setSmallIcon(R.drawable.sym_def_app_icon)
            .build()
        startForeground(notificationId, notification)
    }

    private fun connectProcess(usbDevice: String) {
        var driver: UsbSerialDriver = UsbSerialProber.getDefaultProber().probeDevice(getUsbDevice(usbDevice))
        try {
            val usbManager = applicationContext.getSystemService(USB_SERVICE) as UsbManager
            val usbConnection = usbManager.openDevice(driver.device)
            usbSerialPort = driver.ports[0]
            usbSerialPort.also {
                it?.open(usbConnection)
                it?.setParameters(38400, 8, 1, UsbSerialPort.PARITY_NONE)
            }
            usbIoManager = SerialInputOutputManager(usbSerialPort, this@UsbDeviceConnectService)
            usbIoManager.start()
            CoroutineScope(Dispatchers.IO).launch {
                FlowManager.deviceConnectSharedFlow.emit(
                    DeviceConnectSharedFlow.ConnectCompleteFlow(
                        DeviceContentsVO(
                            DeviceType.Usb,
                            usbDevice
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Log.w("error", "disconnetDevice")
        }
    }

    fun sendData(byteArray: ByteArray?) {1
        val spn = SpannableStringBuilder()
        spn.append("receive ${byteArray?.size} bytes\n")
        spn.append(
            byteArray?.size?.let {
                ResponseSerialCommunicationFormat().toHex(
                    byteArray,
                    it
                )
            }
        ).append("\n")
        Log.w("requestData", spn.toString())
        usbSerialPort.write(byteArray, 0)
    }

    fun disConnect() {
        try {
            usbIoManager.listener = null
            usbIoManager.stop()
            usbSerialPort.close()
        } catch (e: Exception){
        }
    }

    fun init() {
//        responseDeviceSerialCommunication.init()
    }

    override fun onNewData(data: ByteArray) {
        CoroutineScope(Dispatchers.IO).launch {
            FlowManager.deviceSerialCommunicate.emit(DeviceSerialCommunicate.ResultSerial(data))
        }
    }

    override fun onRunError(e: Exception?) {
        val mainLooper = Handler(Looper.getMainLooper())
        mainLooper.post {
            if(e?.message == "USB get_status request failed"){
                stopSelf()
            }
        }
    }

    private inner class UsbPermissionReceiver : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == actionGrantUsb) {
                val usbManager = context.getSystemService(Service.USB_SERVICE) as UsbManager
                val usbDevice = intent.getStringExtra("usbDevice")
                if(usbManager.hasPermission(getUsbDevice(usbDevice!!))){
                    connectProcess(usbDevice!!)
                }
                context.unregisterReceiver(this)
            }
        }
    }

    private fun getUsbDevice(usbDeviceInformation: String): UsbDevice? {
        val usbManager = applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
        for(device in usbManager.deviceList.values) {
            if(stringFormat(device.toString()) == stringFormat(usbDeviceInformation)) {
                return device
            }
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
        CoroutineScope(Dispatchers.IO).launch {
            FlowManager.deviceConnectSharedFlow.emit(
                DeviceConnectSharedFlow.IsDisConnected(boolean = false)
            )
        }
    }
}