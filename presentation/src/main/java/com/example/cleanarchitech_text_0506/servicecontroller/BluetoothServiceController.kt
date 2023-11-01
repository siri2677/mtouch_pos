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
import com.example.cleanarchitech_text_0506.service.BluetoothDeviceConnectServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class BluetoothServiceController(private val context: Context): DeviceServiceController {
    override var deviceConnectSharedFlow = MutableSharedFlow<DeviceConnectSharedFlow>()
//    private var bluetoothDeviceConnectServiceImpl: BluetoothDeviceConnectServiceImpl? = null

    private lateinit var bluetoothDeviceConnectServiceImpl: BluetoothDeviceConnectServiceImpl
    private lateinit var afterBindProcess: (DeviceConnectService) -> Unit
    private lateinit var job: Job

    @RequiresApi(Build.VERSION_CODES.O)
    override fun connect(bluetoothDeviceAddress: String) {
        val intent = Intent(context, bluetoothDeviceConnectServiceImpl::class.java)
        intent.putExtra(DeviceType.Bluetooth.name, bluetoothDeviceAddress)
        context.startForegroundService(intent)
    }

    override fun disConnect() {
        val intent = Intent(context, bluetoothDeviceConnectServiceImpl::class.java)
        context.stopService(intent)
    }

    override fun bindingService(afterBindProcess: (DeviceConnectService) -> Unit) {
        try{
            afterBindProcess(bluetoothDeviceConnectServiceImpl)
        } catch (e: Exception) {
            this.afterBindProcess = afterBindProcess
            val intent = Intent(context, BluetoothDeviceConnectServiceImpl::class.java)
            context.bindService(intent, myServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun unBindingService() {
        try {
            job.cancel()
            context.unbindService(myServiceConnection)
        } catch (e: Exception) {
        }
    }

    private val myServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val binder = binder as BluetoothDeviceConnectServiceImpl.MyBinder
            bluetoothDeviceConnectServiceImpl = binder.getService()
            afterBindProcess(bluetoothDeviceConnectServiceImpl)
//            job = CoroutineScope(Dispatchers.IO).launch(start = CoroutineStart.LAZY) {
//                bluetoothDeviceConnectServiceImpl!!.deviceConnectSharedFlow.collect(){
//                    deviceConnectSharedFlow.emit(it)
//                }
//            }
//            if(!job.isActive){ job.start() }
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    }

}