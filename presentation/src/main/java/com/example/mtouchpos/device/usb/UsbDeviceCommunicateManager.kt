package com.example.mtouchpos.device.usb

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.mtouchpos.device.deviceinterface.DeviceCommunicateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UsbDeviceCommunicateManager(
    private val context: Context
): DeviceCommunicateManager {
    private val usbDeviceConnectServiceImpl = MutableSharedFlow<UsbDeviceConnectService>(replay = 1, extraBufferCapacity = 10)
    private val myServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            (binder as UsbDeviceConnectService.MyBinder).also {
                CoroutineScope(Dispatchers.IO).launch { usbDeviceConnectServiceImpl.emit(it.getService()) }
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    init{ bindingService() }

    override fun bindingService() {
        context.bindService(Intent(context, UsbDeviceConnectService::class.java), myServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun unBindingService() {
        try {
            context.unbindService(myServiceConnection)
        } catch (e: Exception) {
        }
    }

    override fun sendData(byteArray: ByteArray) {
        CoroutineScope(Dispatchers.IO).launch { usbDeviceConnectServiceImpl.collectLatest{ it.sendData(byteArray) } }
    }

}