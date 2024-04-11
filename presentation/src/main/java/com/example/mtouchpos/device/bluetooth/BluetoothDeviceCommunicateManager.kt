package com.example.mtouchpos.device.bluetooth

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.mtouchpos.device.deviceinterface.DeviceCommunicateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.Serializable


class BluetoothDeviceCommunicateManager(
    private val context: Context
): DeviceCommunicateManager, Serializable  {
    private val bluetoothDeviceConnectServiceImpl = MutableSharedFlow<BluetoothDeviceConnectService>(replay = 1, extraBufferCapacity = 1)
    private val myServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            (binder as BluetoothDeviceConnectService.BluetoothServiceBinder).also {
                CoroutineScope(Dispatchers.IO).launch { bluetoothDeviceConnectServiceImpl.emit(it.getService()) }
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    init{ bindingService() }

    override fun bindingService() {
        context.bindService(Intent(context, BluetoothDeviceConnectService::class.java), myServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun unBindingService() {
        try {
            context.unbindService(myServiceConnection)
        } catch (e: Exception) {
        }
    }

    override fun sendData(byteArray: ByteArray) {
        CoroutineScope(Dispatchers.IO).launch { bluetoothDeviceConnectServiceImpl.collect{ it.sendData(byteArray) } }
    }
}