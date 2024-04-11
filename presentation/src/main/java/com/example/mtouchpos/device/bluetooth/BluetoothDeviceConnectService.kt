package com.example.mtouchpos.device.bluetooth

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
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mtouchpos.FlowManager
import com.example.mtouchpos.vo.DeviceContentsVO
import com.example.mtouchpos.vo.DeviceConnectSharedFlow
import com.example.mtouchpos.vo.DeviceSerialCommunicate
import com.example.domain.vo.DeviceType
import com.example.mtouchpos.device.ResponseSerialCommunicationFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class BluetoothDeviceConnectService: Service() {
    private val notificationId = 1
    private val channelId = "MyServiceChannel"

    @SuppressWarnings("MissingPermission")
    private val gattClientCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.w("onMtuChanged", mtu.toString())
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
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
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(ContentValues.TAG, "Device service discovery failed, status: $status")
            } else {
                val respCharacteristic = gatt?.let { findResponseCharacteristic(it) } ?: return
                val descriptor: BluetoothGattDescriptor = respCharacteristic.getDescriptor(
                    UUID.fromString(BluetoothDeviceConstantObject.CLIENT_CHARACTERISTIC_CONFIG)
                )
                gatt?.setCharacteristicNotification(respCharacteristic, true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt?.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt?.writeDescriptor(descriptor)
                }
                CoroutineScope(Dispatchers.IO).launch {
                    FlowManager.deviceConnectSharedFlow.emit(
                        DeviceConnectSharedFlow.ConnectCompleteFlow(
                            DeviceContentsVO(
                                DeviceType.Bluetooth,
                                bluetoothGatt.device.address
                            )
                        )
                    )
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.d(ContentValues.TAG, "Characteristic change successfully, ${characteristic.value}")
            CoroutineScope(Dispatchers.IO).launch {
                FlowManager.deviceSerialCommunicate.emit(DeviceSerialCommunicate.ResultSerial(characteristic.value as ByteArray))
            }
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
    }

    private lateinit var bluetoothGatt: BluetoothGatt

    inner class BluetoothServiceBinder : Binder() {
        fun getService(): BluetoothDeviceConnectService = this@BluetoothDeviceConnectService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return BluetoothServiceBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        intent?.getStringExtra(DeviceType.Bluetooth.name)?.let { connectDevice(it) }
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
            .setContentText("블루투스 연결 진행 상태입니다.")
            .setSmallIcon(R.drawable.sym_def_app_icon)
            .build()
        startForeground(notificationId, notification)
    }

    @SuppressWarnings("MissingPermission")
    fun connectDevice(bluetoothDevice: String) {
        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val device = bluetoothAdapter.getRemoteDevice(bluetoothDevice)
        bluetoothGatt = device.connectGatt(this, false, gattClientCallback)
    }

    @SuppressWarnings("MissingPermission")
    fun sendData(byteArray: ByteArray?){
        val bluetoothGattCharacteristic = findWriteCharacteristic(bluetoothGatt!!)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothGatt!!.writeCharacteristic(bluetoothGattCharacteristic!!, byteArray!!, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
        } else {
            bluetoothGattCharacteristic?.value = byteArray
            bluetoothGatt!!.writeCharacteristic(bluetoothGattCharacteristic!!)
        }
    }

    @SuppressLint("MissingPermission")
    fun disConnect() {
        bluetoothGatt.disconnect()
        bluetoothGatt.close()
    }

    fun init() {
//        responseDeviceSerialCommunication.init()
    }

    private fun findWriteCharacteristic(bluetoothGatt: BluetoothGatt): BluetoothGattCharacteristic? {
        return findCharacteristic(bluetoothGatt,
            BluetoothDeviceConstantObject.CHARACTERISTIC_WRITE_STRING
        )
    }

    private fun findResponseCharacteristic(bluetoothGatt: BluetoothGatt): BluetoothGattCharacteristic? {
        return findCharacteristic(bluetoothGatt,
            BluetoothDeviceConstantObject.CHARACTERISTIC_RESPONSE_STRING
        )
    }

    private fun findCharacteristic(bluetoothGatt: BluetoothGatt, writeOrResponseUuid: String): BluetoothGattCharacteristic?{
        var bluetoothGattService: BluetoothGattService? = null
        for (service in bluetoothGatt.services) {
            val serviceUuidString = service.uuid.toString()
            if (matchUUIDs(serviceUuidString, BluetoothDeviceConstantObject.SERVICE_STRING)) {
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
        CoroutineScope(Dispatchers.IO).launch {
            FlowManager.deviceConnectSharedFlow.emit(
                DeviceConnectSharedFlow.IsDisConnected(boolean = false)
            )
        }
    }

}