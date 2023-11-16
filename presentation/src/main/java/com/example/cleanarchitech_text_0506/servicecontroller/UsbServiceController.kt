package com.example.cleanarchitech_text_0506.servicecontroller

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.cleanarchitech_text_0506.deviceinterface.DeviceConnectService
import com.example.cleanarchitech_text_0506.deviceinterface.DeviceServiceController
import com.example.cleanarchitech_text_0506.enum.DeviceType
import com.example.cleanarchitech_text_0506.sealed.DeviceConnectSharedFlow
import com.example.cleanarchitech_text_0506.service.UsbDeviceConnectServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class UsbServiceController(val context: Context): DeviceServiceController {
    override var deviceConnectSharedFlow = MutableSharedFlow<DeviceConnectSharedFlow>()
    private lateinit var usbDeviceConnectServiceImpl: UsbDeviceConnectServiceImpl
    private lateinit var afterBindProcess: (DeviceConnectService) -> Unit

    @RequiresApi(Build.VERSION_CODES.O)
    override fun connect(usbDevice: String) {
        val intent = Intent(context, usbDeviceConnectServiceImpl::class.java)
        intent.putExtra(DeviceType.Usb.name, usbDevice)
        context.startForegroundService(intent)
    }

    override fun disConnect() {
        if(::usbDeviceConnectServiceImpl.isInitialized) {
            val intent = Intent(context, usbDeviceConnectServiceImpl::class.java)
            context.stopService(intent)
        }
    }

    override fun bindingService(afterBindProcess: (DeviceConnectService) -> Unit) {
        if(::usbDeviceConnectServiceImpl.isInitialized) {
            afterBindProcess(usbDeviceConnectServiceImpl)
        } else {
            this.afterBindProcess = afterBindProcess
            val intent = Intent(context, UsbDeviceConnectServiceImpl::class.java)
            context.bindService(intent, myServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun unBindingService() {
        try {
            context.unbindService(myServiceConnection)
        } catch (e: Exception) {
            Log.w("exception1", e.toString())
        }
    }

    private val myServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val binder = binder as UsbDeviceConnectServiceImpl.MyBinder
            usbDeviceConnectServiceImpl = binder.getService()
            afterBindProcess(binder.getService())
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    }
}