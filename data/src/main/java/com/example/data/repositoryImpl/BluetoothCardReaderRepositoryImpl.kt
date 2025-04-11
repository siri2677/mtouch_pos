package com.example.data.repositoryImpl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.example.data.service.BluetoothCardReaderService
import com.example.data.service.CardReaderResponse
import com.example.domain.repository.CardReaderCommunicateRepository

class BluetoothCardReaderRepositoryImpl(private val context: Context) : CardReaderCommunicateRepository {
    private var bluetoothService: BluetoothCardReaderService? = null

    override val connectionStatus = CardReaderResponse.connectionStatus
    override val dataStream = CardReaderResponse.dataStream

    override fun connect(deviceInfo: String) {
        bluetoothService?.connect(deviceInfo) ?: run {
            val bluetoothDeviceServiceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                    bluetoothService = (binder as BluetoothCardReaderService.BluetoothServiceBinder).getService()
                    bluetoothService?.connect(deviceInfo)
                }

                override fun onServiceDisconnected(name: ComponentName?) {}
            }

            context.bindService(
                Intent(context, BluetoothCardReaderService::class.java),
                bluetoothDeviceServiceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun disConnect() {
        bluetoothService?.run { disConnect() }
    }

    override fun stopRetry() {
        bluetoothService?.run { stopRetry() }
    }

    override fun sendData(byteArray: ByteArray, isPrint: Boolean) {
        bluetoothService?.run { sendData(byteArray, isPrint) }
    }
}