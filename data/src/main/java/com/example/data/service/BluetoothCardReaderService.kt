package com.example.data.service

import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.domain.model.cardreader.CardReaderStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

class BluetoothCardReaderService(): Service() {
    companion object {
        const val SERVICE_STRING = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
        const val CHARACTERISTIC_WRITE_STRING = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
        const val CHARACTERISTIC_RESPONSE_STRING = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"
        const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "MyServiceChannel"
    }

    private val bleManager by lazy { getSystemService(BLUETOOTH_SERVICE) as BluetoothManager }
    private val bleAdapter by lazy { bleManager.adapter }
    private val notificationManager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }
    private var writeData: ByteArray? = null
    private var retryCount = 0
    lateinit var connectReaderJob: Job
    lateinit var bluetoothGatt: BluetoothGatt

    inner class BluetoothServiceBinder : Binder() {
        fun getService(): BluetoothCardReaderService = this@BluetoothCardReaderService
    }

    inner class Action1Receiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            TODO("Not yet implemented")
        }
    }

    inner class Action2Receiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            TODO("Not yet implemented")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return BluetoothServiceBinder()
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
            .setContentText("Bluetooth 연결 진행 상태입니다.")
            .setSmallIcon(R.drawable.sym_def_app_icon)
            .addAction(R.drawable.sym_def_app_icon, "Action 1", action1PendingIntent)
            .addAction(R.drawable.sym_def_app_icon, "Action 2", action2PendingIntent)
            .build()
        startForeground(NOTIFICATION_ID, notificationCompat)
    }

    @SuppressLint("MissingPermission")
    fun connect(deviceInfo: String) {
        val isConnected = bleManager.getConnectedDevices(BluetoothProfile.GATT).any { it.address == deviceInfo }
        if(isConnected) {
            stopRetry()
            createNotificationChannel()
            CardReaderResponse.connectionStatus.emitWithInCoroutine(CardReaderStatus.Connection.Active)
            return
        }

        if(retryCount == 0) CardReaderResponse.connectionStatus.emitWithInCoroutine(CardReaderStatus.Connection.Establishing(retryCount))

        connectReaderJob = CoroutineScope(Dispatchers.IO).launch {
            delay(3000)
            CardReaderResponse.connectionStatus.emitWithInCoroutine(CardReaderStatus.Connection.Establishing(++retryCount))
            connect(deviceInfo)
        }

        bluetoothGatt = bleAdapter.getRemoteDevice(deviceInfo).connectGatt(this, false, getBluetoothGattCallback())
    }


    @SuppressWarnings("MissingPermission")
    private fun getBluetoothGattCallback() = object : BluetoothGattCallback() {
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
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) return
            gatt?.let { gatt ->
                findCharacteristic(gatt, CHARACTERISTIC_RESPONSE_STRING).let {
                    gatt.setCharacteristicNotification(it, true)
                    it?.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))
                }?.also {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        gatt.writeDescriptor(it, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    } else {
                        it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(it)
                    }
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Log.d("write", "status: " + status + " characteristic: uuid : " + characteristic.uuid + " value: " + characteristic.value)
            CoroutineScope(Dispatchers.IO).launch {
                delay(1500)
                writeData?.let {
                    if(it[3].toString() == "-64") { CardReaderResponse.connectionStatus.emit(CardReaderStatus.Connection.Active) }
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            Log.d(ContentValues.TAG, "Characteristic change successfully, $value")
            writeData = null
            CardReaderResponse.dataStream.emitWithInCoroutine(value)
        }
    }

    @SuppressLint("MissingPermission")
    fun disConnect() {
        if(::bluetoothGatt.isInitialized) {
            stopRetry()
            stopForeground(STOP_FOREGROUND_REMOVE)
            bluetoothGatt.disconnect()
            bluetoothGatt.close()
        }
        CardReaderResponse.connectionStatus.emitWithInCoroutine(CardReaderStatus.Connection.Inactive)
    }

    fun stopRetry() {
        retryCount = 0
        if(::connectReaderJob.isInitialized) connectReaderJob.cancel()
    }

    @SuppressLint("MissingPermission")
    fun sendData(byteArray: ByteArray) {
        writeData = byteArray
        findCharacteristic(
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

    private fun <T> MutableSharedFlow<T>.emitWithInCoroutine(cardReaderStatus: T) {
        CoroutineScope(Dispatchers.IO).launch { this@emitWithInCoroutine.emit(cardReaderStatus) }
    }

    private fun findCharacteristic(
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