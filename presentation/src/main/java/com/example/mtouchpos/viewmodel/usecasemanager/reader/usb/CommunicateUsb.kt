package com.example.mtouchpos.viewmodel.usecasemanager.reader.usb

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.domain.model.device.DeviceCommunicateResponseData
import com.example.domain.usecase.device.manager.CommunicateDeviceManager
import com.example.mtouchpos.service.UsbService
import com.example.mtouchpos.viewmodel.usecasemanager.reader.DeviceCommunicateResponseDataImpl
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
): CommunicateDeviceManager {
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

    override fun isDeviceServiceInitialized() = usbDeviceConnectServiceImpl.value.run {
        if(this is UsbService) {
            isUsbGattInitialized()
        } else {
            false
        }
    }

    override fun connect(deviceInfo: String) = flow {
        with(usbDeviceConnectServiceImpl.value) {
            connectDevice(deviceInfo)

            mutex.withLock {
                DeviceCommunicateResponseDataImpl.deviceConnectStatus.collect { emit(it) }
            }
        }
    }

    override fun sendData(byteArray: ByteArray) {
        with(usbDeviceConnectServiceImpl.value) {
            if(this is UsbService) usbSerialPort.write(byteArray, 0)
        }
    }

}