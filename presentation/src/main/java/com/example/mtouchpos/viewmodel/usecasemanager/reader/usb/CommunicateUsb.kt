package com.example.mtouchpos.viewmodel.usecasemanager.reader.usb

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.domain.usecase.device.manager.CommunicateDeviceManager
import com.example.mtouchpos.service.UsbService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CommunicateUsb(
    private val context: Context
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

    override fun sendData(byteArray: ByteArray) {
        with(usbDeviceConnectServiceImpl.value) {
            if(this is UsbService) {
                usbSerialPort.write(byteArray, 0)
            }
        }
    }

}