package com.example.mtouchpos.service

import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mtouchpos.managerImpl.cardreader.CardReaderResponseImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class BluetoothService: Service() {
    companion object {
        const val BLUETOOTH_DEVICE = "bluetoothDevice"
        const val SERVICE_STRING = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
        const val CHARACTERISTIC_WRITE_STRING = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
        const val CHARACTERISTIC_RESPONSE_STRING = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"
        const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
    }

    object FindCharacteristic {
        operator fun invoke(
            bluetoothGatt: BluetoothGatt,
            writeOrResponseUuid: String
        ): BluetoothGattCharacteristic? = bluetoothGatt.services
            .filter { service ->
                matchUUIDs(service.uuid.toString(), SERVICE_STRING)
            }
            .flatMap { service ->
                service.characteristics.filter { characteristic ->
                    matchUUIDs(characteristic.uuid.toString(), writeOrResponseUuid)
                }
            }
            .firstOrNull()

        private fun matchUUIDs(
            uuidString: String,
            vararg matches: String
        ): Boolean = matches.any { it.equals(uuidString, ignoreCase = true) }
    }

    inner class BluetoothServiceBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    private val notificationId = 1
    private val channelId = "MyServiceChannel"
    private var writeData: ByteArray? = null
    var retryCount = 0
    lateinit var connectReader: Job
    lateinit var bluetoothGatt: BluetoothGatt

    fun isBluetoothGattInitialized() = ::bluetoothGatt.isInitialized

    override fun onBind(intent: Intent?): IBinder? {
        return BluetoothServiceBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        intent?.getStringExtra(BLUETOOTH_DEVICE)?.let { connectDevice(it) }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(channelId, "My Service Channel", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Channel for My Service"
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }

        startForeground(
            notificationId,
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("mtouch앱 실행중입니다.")
                .setContentText("블루투스 연결 진행 상태입니다.")
                .setSmallIcon(R.drawable.sym_def_app_icon)
                .build()
        )
    }

    @SuppressWarnings("MissingPermission")
    fun checkConnect(bluetoothDevice: String): Boolean {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).any {
            it.address == bluetoothDevice
        }
    }

    @SuppressWarnings("MissingPermission")
    fun connectDevice(bluetoothDevice: String) {
        if(checkConnect(bluetoothDevice)) {
            CardReaderResponseImpl.onConnected()
            stopRetry(null)
            return
        }

        val callback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when(newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d("STATE_CONNECTED", "STATE_CONNECTED")
                        gatt.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d("STATE_DISCONNECTED", "STATE_DISCONNECTED")
                        gatt.disconnect()
                        gatt.close()
                        stopSelf()
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) return
                gatt?.let { gatt ->
                    FindCharacteristic(gatt, CHARACTERISTIC_RESPONSE_STRING).let {
                        gatt.setCharacteristicNotification(it, true)
                        it?.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))
                    }?.also {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt?.writeDescriptor(it, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        } else {
                            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt?.writeDescriptor(it)
                        }
                        Log.d("onServicesDiscovered", "onServicesDiscovered")
                        CardReaderResponseImpl.onConnected()
                    }
                }
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                Log.d("write", "status: " + status + " characteristic: uuid : " + characteristic?.uuid + " value: " + characteristic?.value)
                CoroutineScope(Dispatchers.IO).launch {
                    delay(1500)
                    writeData?.let { if(it[3].toString() == "-64") CardReaderResponseImpl.onConnected() }
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                writeData = null
                Log.d(ContentValues.TAG, "Characteristic change successfully, $value")
                CardReaderResponseImpl.onResultCommunicate(value)
            }
        }

        connectReader = CoroutineScope(Dispatchers.IO).launch {
            CardReaderResponseImpl.onDisConnected(retryCount++)
            delay(3000)
            connectDevice(bluetoothDevice)
        }

        bluetoothGatt = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
            .getRemoteDevice(bluetoothDevice)
            .connectGatt(this, false, callback)
    }

    @SuppressLint("MissingPermission")
    fun sendData(byteArray: ByteArray) {
        writeData = byteArray
        FindCharacteristic(
            bluetoothGatt,
            CHARACTERISTIC_WRITE_STRING
        )?.also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bluetoothGatt.writeCharacteristic(it, byteArray, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            } else {
                it.value = byteArray
                bluetoothGatt.writeCharacteristic(it)
            }
        }
    }

    fun stopRetry(byteArray: ByteArray?) {
        byteArray?.let { sendData(it) }
        retryCount = 0
        if(::connectReader.isInitialized) connectReader.cancel()
    }

    @SuppressLint("MissingPermission")
    fun disConnect() {
        bluetoothGatt.disconnect()
        bluetoothGatt.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            disConnect()
        } catch (e: Exception){
        }
    }

}