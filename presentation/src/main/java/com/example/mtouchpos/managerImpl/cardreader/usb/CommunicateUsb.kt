package com.example.mtouchpos.managerImpl.cardreader.usb

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.domain.manager.cardreader.CardReaderCommunicateManager
import com.example.mtouchpos.managerImpl.cardreader.CardReaderResponseImpl
import com.example.mtouchpos.service.UsbService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CommunicateUsb(
    private val context: Context,
    private val mutex: Mutex
): CardReaderCommunicateManager {
    private val usbDeviceConnectServiceImpl = MutableStateFlow(UsbService())
    private val usbDeviceServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            (binder as UsbService.MyBinder).also {
                CoroutineScope(Dispatchers.IO).launch { usbDeviceConnectServiceImpl.emit(it.getService()) }
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    init{ bindingService() }

    override fun bindingService() {
        context.bindService(Intent(context, UsbService::class.java), usbDeviceServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun unBindingService() {
        try {
            context.unbindService(usbDeviceServiceConnection)
        } catch (e: Exception) {
        }
    }

    override fun stopRetry(byteArray: ByteArray) {
        usbDeviceConnectServiceImpl.value.stopRetry(byteArray)
    }

//    override fun isDeviceServiceInitialized() = usbDeviceConnectServiceImpl.value.run {
//        if(this is UsbService) {
//            isUsbGattInitialized()
//        } else {
//            false
//        }
//    }

    override fun connect(deviceInfo: String) {
        usbDeviceConnectServiceImpl.value.connect(deviceInfo)
//        mutex.withLock {
//            CardReaderResponseImpl.deviceConnectStatus.collect { emit(it) }
//        }
    }

    override fun sendData(byteArray: ByteArray) {
        usbDeviceConnectServiceImpl.value.sendData(byteArray)
    }
}