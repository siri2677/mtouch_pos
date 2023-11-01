package com.example.cleanarchitech_text_0506.service

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
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.cleanarchitech_text_0506.enum.SerialCommunicationMessage
import com.example.cleanarchitech_text_0506.deviceinterface.DeviceConnectService
import com.example.cleanarchitech_text_0506.sealed.DeviceConnectSharedFlow
import com.example.cleanarchitech_text_0506.enum.DeviceType
import com.example.cleanarchitech_text_0506.util.EncMSRManager
import com.example.cleanarchitech_text_0506.util.ResponseSerialCommunicationFormat
import com.example.cleanarchitech_text_0506.vo.KsnetSocketCommunicationDTO
import com.example.domain.dto.request.tms.RequestPaymentDTO
import com.example.domain.usecaseinterface.ResponseDeviceSerialCommunication
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UsbDeviceConnectServiceImpl: Service(), DeviceConnectService, SerialInputOutputManager.Listener {
    private val actionGrantUsb = "ACTION_GRANT_USB"
    private val notificationId = 1
    private val channelId = "MyServiceChannel"
    private lateinit var usbDevice: String
    lateinit var job: Job

    private lateinit var usbSerialPort: UsbSerialPort
    private lateinit var usbIoManager: SerialInputOutputManager
//    @Inject lateinit var responseDeviceSerialCommunication: ResponseDeviceSerialCommunication

    lateinit var ksnetSocketCommunicationDTO: KsnetSocketCommunicationDTO
    lateinit var deviceConnectSharedFlow: MutableSharedFlow<DeviceConnectSharedFlow>
    val responseSerialCommunicationFormat = ResponseSerialCommunicationFormat()


//    override var isFirstConnectComplete = MutableSharedFlow<Boolean>()
//    override var isCompletePayment = MutableSharedFlow<Boolean>()
//    override var serialCommunicationMessage = MutableSharedFlow<String>()
//    override var permissionCheckComplete = MutableSharedFlow<Boolean>()

    inner class MyBinder : Binder() {
        fun getService(): UsbDeviceConnectServiceImpl = this@UsbDeviceConnectServiceImpl
    }

    override fun onBind(intent: Intent?): IBinder? {
        return MyBinder()
    }

    override fun setDeviceConnectSharedFlow(
        deviceConnectSharedFlow: MutableSharedFlow<DeviceConnectSharedFlow>
    ): DeviceConnectService {
        this.deviceConnectSharedFlow = deviceConnectSharedFlow
        return this
    }

    override fun setEssentialData(
        ksnetSocketCommunicationDTO: KsnetSocketCommunicationDTO,
        deviceConnectSharedFlow: MutableSharedFlow<DeviceConnectSharedFlow>
    ): DeviceConnectService {
        this.ksnetSocketCommunicationDTO = ksnetSocketCommunicationDTO
        this.deviceConnectSharedFlow = deviceConnectSharedFlow
        return this
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
        intent?.getStringExtra(DeviceType.Usb.name)?.let { connectDevice(it, ByteArray(0)) }
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

//    private fun flowCollect() {
//        job = CoroutineScope(Dispatchers.IO).launch(start = CoroutineStart.LAZY) {
//            responseDeviceSerialCommunication.afterProcess.collect {
//                when (it.type) {
//                    ProcessAfterSerialCommunicate.RequestCardInsert.name -> {
//                        deviceConnectSharedFlow.emit(
//                            DeviceConnectSharedFlow.SerialCommunicationMessageFlow(
//                                SerialCommunicationMessage.IcCardInsertRequest.message
//                            )
//                        )
//                    }
//                    ProcessAfterSerialCommunicate.RequestCardNumber.name -> {
//                        deviceConnectSharedFlow.emit(
//                            DeviceConnectSharedFlow.SerialCommunicationMessageFlow(
//                                SerialCommunicationMessage.PaymentProgressing.message
//                            )
//                        )
//                        sendData(
//                            EncMSRManager().makeCardNumSendReq(
//                                "00000001004".toByteArray(),
//                                "10".toByteArray()
//                            )
//                        )
//                    }
//                    ProcessAfterSerialCommunicate.RequestFallback.name -> {
//                        deviceConnectSharedFlow.emit(
//                            DeviceConnectSharedFlow.SerialCommunicationMessageFlow(
//                                SerialCommunicationMessage.FallBackMessage.message
//                            )
//                        )
//                        sendData(EncMSRManager().makeFallBackCardReq(it.data!!, "99"))
//                    }
//                    ProcessAfterSerialCommunicate.NoticeCompletePayment.name -> {
//                        deviceConnectSharedFlow.emit(
//                            DeviceConnectSharedFlow.SerialCommunicationMessageFlow(
//                                SerialCommunicationMessage.CompletePayment.message
//                            )
//                        )
//                        delay(300)
//                        deviceConnectSharedFlow.emit(
//                            DeviceConnectSharedFlow.PaymentCompleteFlow(true)
//                        )
//                    }
//                }
//            }
//        }
//        if(!job.isActive){ job.start() }
//    }

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
            usbIoManager = SerialInputOutputManager(usbSerialPort, this@UsbDeviceConnectServiceImpl)
            usbIoManager.start()
            CoroutineScope(Dispatchers.IO).launch {
                deviceConnectSharedFlow.emit(
                    DeviceConnectSharedFlow.PermissionCheckCompleteFlow(true)
                )
            }
//                permissionCheckComplete.emit(true)
//            usbIoManager?.state?.name?.let { Log.w("StateRunning", it) }
        } catch (e: Exception) {
            Log.w("error", "disconnetDevice")
        }
    }



    override fun connectDevice(usbDevice: String, byteArray: ByteArray) {
        val usbManager = applicationContext.getSystemService(USB_SERVICE) as UsbManager
        try {
            if(usbManager.hasPermission(getUsbDevice(usbDevice))){
                connectProcess(usbDevice)
            } else {
                applicationContext.registerReceiver(UsbPermissionReceiver(), IntentFilter(actionGrantUsb))
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                val intent = Intent(actionGrantUsb)
                intent.putExtra("usbDevice", usbDevice)
                val permissionIntent: PendingIntent =
                    PendingIntent.getBroadcast(applicationContext, 0, intent, flags)
                usbManager.requestPermission(getUsbDevice(usbDevice), permissionIntent)
            }
        } catch (e: Exception){
            Toast.makeText(this, "결제가 완료 되었습니다", Toast.LENGTH_LONG).show()
        }
    }

    override fun sendData(byteArray: ByteArray?) {
//        flowCollect()
        try {
            usbSerialPort.write(byteArray, 0)
        } catch (e: Exception) {
        }
    }

    override fun disConnect() {
        try {
            usbIoManager.listener = null
            usbIoManager.stop()
            usbSerialPort.close()
        } catch (e: Exception){
        }
    }

    override fun init() {
//        responseDeviceSerialCommunication.init()
    }

    override fun onNewData(data: ByteArray) {
        CoroutineScope(Dispatchers.IO).launch {
            responseSerialCommunicationFormat.receiveData(data,ksnetSocketCommunicationDTO, deviceConnectSharedFlow)
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

    private fun getUsbDevice(usbDeviceInformation: String): UsbDevice? {
        val usbManager = applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
        Log.w("ListSize", usbManager.deviceList.size.toString())
        for(device in usbManager.deviceList.values) {
            if(stringFormat(device.toString()) == stringFormat(usbDeviceInformation)) {
                Log.w("device", device.toString())
                return device
            }
        }
        return null
    }

    private fun stringFormat(string: String): String {
        val regex = Regex("""mSerialNumberReader=[^,]+""")
        return regex.replace(string, "")
    }

    private inner class UsbPermissionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == actionGrantUsb) {
                val usbManager = applicationContext.getSystemService(USB_SERVICE) as UsbManager
                val usbDevice = intent.getStringExtra("usbDevice")
                if(usbManager.hasPermission(getUsbDevice(usbDevice!!))){
                    connectProcess(usbDevice)
                }
                applicationContext.unregisterReceiver(this)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            disConnect()
            job.cancel()
        } catch (e: Exception){
        }
        CoroutineScope(Dispatchers.IO).launch {
            deviceConnectSharedFlow.emit(
                DeviceConnectSharedFlow.ConnectCompleteFlow(false)
            )
        }
    }
}