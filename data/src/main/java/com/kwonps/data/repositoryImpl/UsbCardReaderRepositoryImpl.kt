package com.kwonps.data.repositoryImpl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.kwonps.data.service.CardReaderResponse
import com.kwonps.data.service.UsbCardReaderService
import com.kwonps.domain.repository.CardReaderCommunicateRepository

class UsbCardReaderRepositoryImpl(private val context: Context) : CardReaderCommunicateRepository {
    private var usbService: UsbCardReaderService? = null

    override val connectionStatus = CardReaderResponse.connectionStatus
    override val dataStream = CardReaderResponse.dataStream

    override fun connect(deviceInfo: String) {
        usbService?.connect(deviceInfo) ?: run {
            val usbDeviceServiceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                    usbService = (binder as UsbCardReaderService.UsbServiceBinder).getService()
                    usbService?.connect(deviceInfo)
                }

                override fun onServiceDisconnected(name: ComponentName?) {}
            }

            context.bindService(
                Intent(context, UsbCardReaderService::class.java),
                usbDeviceServiceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    override fun disConnect() {
        usbService?.run { disConnect() }
    }

    override fun stopRetry() {
        usbService?.run { stopRetry() }
    }

    override fun sendData(byteArray: ByteArray, isPrint: Boolean) {
        usbService?.run { sendData(byteArray) }
    }
}