package com.example.cleanarchitech_text_0506.service

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.cleanarchitech_text_0506.deviceinterface.DeviceConnectService
import com.example.cleanarchitech_text_0506.enum.DeviceType
import com.example.cleanarchitech_text_0506.sealed.DeviceConnectSharedFlow
import com.example.cleanarchitech_text_0506.util.ResponseSerialCommunicationFormat
import com.example.cleanarchitech_text_0506.vo.DomainLayerConstantObject
import com.example.cleanarchitech_text_0506.vo.KsnetSocketCommunicationDTO
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class BluetoothDeviceConnectServiceImpl: Service(), DeviceConnectService  {
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "MyServiceChannel"
    private lateinit var bluetoothGatt: BluetoothGatt
//    @Inject lateinit var responseDeviceSerialCommunication: ResponseDeviceSerialCommunication

    lateinit var ksnetSocketCommunicationDTO: KsnetSocketCommunicationDTO
    lateinit var byteArray: ByteArray
    lateinit var deviceConnectSharedFlow: MutableSharedFlow<DeviceConnectSharedFlow>

    val responseSerialCommunicationFormat = ResponseSerialCommunicationFormat()

    inner class MyBinder : Binder() {
        fun getService(): BluetoothDeviceConnectServiceImpl = this@BluetoothDeviceConnectServiceImpl
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        intent?.getStringExtra(DeviceType.Bluetooth.name)?.let { connectDevice(it, ByteArray(0)) }
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
            .setContentText("블루투스 연결 진행 상태입니다.")
            .setSmallIcon(R.drawable.sym_def_app_icon)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

//    private fun flowCollect() {
//        job = CoroutineScope(Dispatchers.IO).launch(start = CoroutineStart.LAZY) {
//            responseSerialCommunicationFormat.afterProcess.collect {
//                when (it.type) {
//                    ProcessAfterSerialCommunicate.RequestCardInsert.name -> {
//                        deviceConnectSharedFlow.emit(
//                            DeviceConnectSharedFlow.SerialCommunicationMessageFlow(SerialCommunicationMessage.IcCardInsertRequest.message)
//                        )
//                    }
//                    ProcessAfterSerialCommunicate.RequestCardNumber.name -> {
//                        deviceConnectSharedFlow.emit(
//                            DeviceConnectSharedFlow.SerialCommunicationMessageFlow(SerialCommunicationMessage.PaymentProgressing.message)
//                        )
//                        sendData(
//                            EncMSRManager().makeCardNumSendReq(
//                                requestInsertPaymentDataDTO.amount.toString().toByteArray(),
//                                "10".toByteArray()
//                            )
//                        )
//                    }
//                    ProcessAfterSerialCommunicate.RequestFallback.name -> {
//                        deviceConnectSharedFlow.emit(
//                            DeviceConnectSharedFlow.SerialCommunicationMessageFlow(SerialCommunicationMessage.FallBackMessage.message)
//                        )
//                        sendData(
//                            EncMSRManager().makeFallBackCardReq(it.data!!, "99")
//                        )
//                    }
//                    ProcessAfterSerialCommunicate.NoticeCompletePayment.name -> {
//                        deviceConnectSharedFlow.emit(
//                            DeviceConnectSharedFlow.SerialCommunicationMessageFlow(SerialCommunicationMessage.CompletePayment.message)
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

    @SuppressWarnings("MissingPermission")
    override fun connectDevice(bluetoothDevice: String, byteArray: ByteArray) {
        this.byteArray = byteArray
        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val device = bluetoothAdapter.getRemoteDevice(bluetoothDevice)
        bluetoothGatt = device.connectGatt(this, false, gattClientCallback)
    }

    override fun sendData(byteArray: ByteArray?){
//        flowCollect()
        if(::bluetoothGatt.isInitialized) {
            val bluetoothGattCharacteristic = findWriteCharacteristic(bluetoothGatt!!)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bluetoothGatt!!.writeCharacteristic(bluetoothGattCharacteristic!!, byteArray!!, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                } else {
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

//                Log.w("sendData", byteArray.toString())
                    bluetoothGattCharacteristic?.value = byteArray
                    bluetoothGatt!!.writeCharacteristic(bluetoothGattCharacteristic!!)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun disConnect() {
        bluetoothGatt.disconnect()
        bluetoothGatt.close()
    }

    override fun init() {
//        responseDeviceSerialCommunication.init()
    }

    private val gattClientCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.w("onMtuChanged", mtu.toString())
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (ActivityCompat.checkSelfPermission(
                    this@BluetoothDeviceConnectServiceImpl,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if( status == BluetoothGatt.GATT_FAILURE ) {
                    Log.w("GATT_FAILURE", "Disconnected from GATT server.")
                } else if( status != BluetoothGatt.GATT_SUCCESS ) {
                    Log.w("GATT_SUCCESS", "Disconnected from GATT server.")
                }
                if( newState == BluetoothProfile.STATE_CONNECTED ) {
                    Log.w("connect", "Disconnected from GATT server.")
                    gatt.discoverServices()
                } else if ( newState == BluetoothProfile.STATE_DISCONNECTED ) {
                    Log.w("disconnect", "Disconnected from GATT server.")
                    gatt.disconnect()
                    gatt.close()
                    stopSelf()
                }
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(ContentValues.TAG, "Device service discovery failed, status: $status")
                return
            }
            val respCharacteristic = gatt?.let { findResponseCharacteristic(it) } ?: return
            if (ActivityCompat.checkSelfPermission(
                    this@BluetoothDeviceConnectServiceImpl,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                gatt?.setCharacteristicNotification(respCharacteristic, true)
                val descriptor: BluetoothGattDescriptor = respCharacteristic.getDescriptor(
                    UUID.fromString(DomainLayerConstantObject.CLIENT_CHARACTERISTIC_CONFIG)
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt?.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt?.writeDescriptor(descriptor)
                }
            }
            CoroutineScope(Dispatchers.IO).launch {
                if (byteArray.isNotEmpty()) {
                    delay(1800)
                    sendData(byteArray)
                } else {
                    delay(600)
                    deviceConnectSharedFlow.emit(DeviceConnectSharedFlow.ConnectCompleteFlow(true))
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            readCharacteristic(characteristic)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(ContentValues.TAG, "Characteristic written successfully, status: $status")
            } else {
                Log.e(ContentValues.TAG, "Characteristic write unsuccessful, status: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(ContentValues.TAG, "Characteristic read successfully")
                readCharacteristic(characteristic)
            } else {
                Log.e(ContentValues.TAG, "Characteristic read unsuccessful, status: $status")
            }
        }
    }

    private fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        Log.w("readCharacteristic", "Characteristic read successfully")
        CoroutineScope(Dispatchers.IO).launch {
            responseSerialCommunicationFormat.receiveData(characteristic.value as ByteArray,ksnetSocketCommunicationDTO, deviceConnectSharedFlow)
        }
    }

    private fun findWriteCharacteristic(bluetoothGatt: BluetoothGatt): BluetoothGattCharacteristic? {
        return findCharacteristic(bluetoothGatt,
            DomainLayerConstantObject.CHARACTERISTIC_WRITE_STRING
        )
    }

    private fun findResponseCharacteristic(bluetoothGatt: BluetoothGatt): BluetoothGattCharacteristic? {
        return findCharacteristic(bluetoothGatt,
            DomainLayerConstantObject.CHARACTERISTIC_RESPONSE_STRING
        )
    }

    private fun findCharacteristic(bluetoothGatt: BluetoothGatt, writeOrResponseUuid: String): BluetoothGattCharacteristic?{
        var bluetoothGattService: BluetoothGattService? = null
        for (service in bluetoothGatt.services) {
            val serviceUuidString = service.uuid.toString()
            if (matchUUIDs(serviceUuidString, DomainLayerConstantObject.SERVICE_STRING)) {
                bluetoothGattService = service
            }
        }
        if(bluetoothGattService != null) {
            for (characteristic in bluetoothGattService?.characteristics!!) {
                val characteristicUuidString = characteristic.uuid.toString()
                if (matchUUIDs(characteristicUuidString, writeOrResponseUuid)) {
                    return characteristic
                }
            }
        }
        return null
    }

    private fun matchUUIDs(uuidString: String, vararg matches: String): Boolean {
        for (match in matches) {
            if (uuidString.equals(match, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            disConnect()
        } catch (e: Exception){
        }
        if(::deviceConnectSharedFlow.isInitialized) {
            CoroutineScope(Dispatchers.IO).launch {
                deviceConnectSharedFlow.emit(
                    DeviceConnectSharedFlow.ConnectCompleteFlow(false)
                )
            }
        }
    }

}