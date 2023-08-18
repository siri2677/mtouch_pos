package com.example.domain.service

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.*
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.*
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.domain.model.DomainLayerConstantObject.Companion.CHARACTERISTIC_RESPONSE_STRING
import com.example.domain.model.DomainLayerConstantObject.Companion.CHARACTERISTIC_WRITE_STRING
import com.example.domain.model.DomainLayerConstantObject.Companion.CLIENT_CHARACTERISTIC_CONFIG
import com.example.domain.model.DomainLayerConstantObject.Companion.SERVICE_STRING
import com.example.domain.usecase.DeviceSettingSharedPreferenceImpl
import com.example.domain.usecase.KsnetUtils
import com.example.domain.usecase.ResponseDeviceSerialCommunicationImpl
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.*


class BluetoothConnectService: Service(){
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "MyServiceChannel"
    }

//    private lateinit var localBroadcastManager: LocalBroadcastManager
    private lateinit var bluetoothGatt: BluetoothGatt

    private var mActivityMessenger: Messenger? = null
    private lateinit var mServiceMessenger: Messenger
    val dataSubject: PublishSubject<ByteArray> = PublishSubject.create()
    var isAfterConnectComplete: PublishSubject<Boolean> = PublishSubject.create()
    var isFirstConnectComplete: PublishSubject<Boolean> = PublishSubject.create()
    private lateinit var player: MediaPlayer

    private val binder = MyBinder()
//
    inner class MyBinder : Binder() {
        fun getService(): BluetoothConnectService = this@BluetoothConnectService
    }

    override fun onBind(intent: Intent): IBinder {
        return MyBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        connectDevice()
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
            .setSmallIcon(dagger.android.support.R.drawable.notification_bg)
            .build()
        startForeground(NOTIFICATION_ID, notification)
//        notificationManager.notify(0, notification)

//        startForeground(NOTIFICATION_ID, notification)
    }

    fun connectDevice() {
        Log.w("bluetoothConnect", "bluetoothConnect")
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val device = bluetoothAdapter.getRemoteDevice(DeviceSettingSharedPreferenceImpl(this).getBluetoothDeviceInformation())
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothGatt = device.connectGatt(this, false, gattClientCallback)
        }
    }

    fun sendData(byteArray: ByteArray?){
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
                        KsnetUtils().toHex(
                            byteArray,
                            it
                        )
                    }
                ).append("\n")
//               spn.append(HexDump.dumpHexString(data)).append("\n")
                Log.w("requestData", spn.toString())

//                Log.w("sendData", byteArray.toString())
                bluetoothGattCharacteristic?.value = byteArray
                bluetoothGatt!!.writeCharacteristic(bluetoothGattCharacteristic!!)
            }
        }
    }

    private val gattClientCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.w("onMtuChanged", mtu.toString())
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (ActivityCompat.checkSelfPermission(
                    this@BluetoothConnectService,
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
                    if(DeviceSettingSharedPreferenceImpl(this@BluetoothConnectService).isKeepBluetoothConnection()) {
                        isFirstConnectComplete.onNext(false)
                    } else {
                        isAfterConnectComplete.onNext(false)
                    }
                    Log.w("disconnect", "Disconnected from GATT server.")
                    gatt.disconnect()
                    gatt.close()
                }
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(ContentValues.TAG, "Device service discovery failed, status: $status")
                return
            }
            val respCharacteristic = gatt?.let { findResponseCharacteristic(it) }
            if( respCharacteristic == null ) {
                stopSelf()
                return
            }
            if (ActivityCompat.checkSelfPermission(
                    this@BluetoothConnectService,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                gatt?.setCharacteristicNotification(respCharacteristic, true)
                val descriptor: BluetoothGattDescriptor = respCharacteristic.getDescriptor(
                    UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG)
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt?.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt?.writeDescriptor(descriptor)
                }
            }
            val sharedPreferences = this@BluetoothConnectService.getSharedPreferences("DeviceInfomation", Context.MODE_PRIVATE)
            if(!sharedPreferences.getBoolean("maintainBluetooth", false)){
                if(DeviceSettingSharedPreferenceImpl(this@BluetoothConnectService).isKeepBluetoothConnection()) {
                    isFirstConnectComplete.onNext(true)
                } else {
                    isAfterConnectComplete.onNext(true)
                }
//                Handler(Looper.getMainLooper()).postDelayed({
//                    connectComplete.onNext(true)
//                },2000)
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
                stopSelf()
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
        dataSubject.onNext(characteristic.value as ByteArray)
    }

    private fun findWriteCharacteristic(bluetoothGatt: BluetoothGatt): BluetoothGattCharacteristic? {
        return findCharacteristic(bluetoothGatt, CHARACTERISTIC_WRITE_STRING)
    }

    private fun findResponseCharacteristic(bluetoothGatt: BluetoothGatt): BluetoothGattCharacteristic? {
        return findCharacteristic(bluetoothGatt, CHARACTERISTIC_RESPONSE_STRING)
    }

    private fun findCharacteristic(bluetoothGatt: BluetoothGatt, writeOrResponseUuid: String): BluetoothGattCharacteristic?{
        var bluetoothGattService: BluetoothGattService? = null
        for (service in bluetoothGatt.services) {
            val serviceUuidString = service.uuid.toString()
            if (matchUUIDs(serviceUuidString, SERVICE_STRING)) {
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
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothGatt.disconnect()
            bluetoothGatt.close()
        }
        if(!DeviceSettingSharedPreferenceImpl(this).isBluetoothDisConnectButtonClick()) {
//            BluetoothDeviceConnectUseCase(this).bluetoothDeviceConnect()
        } else {
            DeviceSettingSharedPreferenceImpl(this).setBluetoothDisConnectButtonClick(false)
//            DeviceSettingSharedPreferenceImpl(this).clearBluetoothDeviceInformation()
        }
        this.stopSelf()
    }
}