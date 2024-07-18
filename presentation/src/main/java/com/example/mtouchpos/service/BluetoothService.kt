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
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mtouchpos.viewmodel.usecasemanager.reader.DeviceCommunicateResponseDataImpl
import dagger.hilt.android.AndroidEntryPoint
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
                .setContentText("블루투스 연결 진행 상태입니다.")
                .setSmallIcon(R.drawable.sym_def_app_icon)
                .build()
        )
    }

    @SuppressWarnings("MissingPermission")
    fun connectDevice(bluetoothDevice: String) {
        bluetoothGatt = (this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
            .getRemoteDevice(bluetoothDevice)
            .connectGatt(
                this,
                false,
                object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                        when(newState) {
                            BluetoothProfile.STATE_CONNECTED -> gatt.discoverServices()
                            BluetoothProfile.STATE_DISCONNECTED -> {
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
                            }
                        }
                        DeviceCommunicateResponseDataImpl.onConnected()
                    }
                    override fun onCharacteristicChanged(
                        gatt: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic
                    ) {
                        Log.d(ContentValues.TAG, "Characteristic change successfully, ${characteristic.value as ByteArray}")
                        DeviceCommunicateResponseDataImpl.onResultCommunicate(characteristic.value as ByteArray)
                    }
                }
            )
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
//        DeviceOperationCallbackImpl.onDisConnected()
    }

}