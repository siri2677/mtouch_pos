package com.example.mtouchpos.viewmodel.usecasemanager.reader.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import com.example.domain.model.device.DeviceCommunicateResponseData
import com.example.domain.usecase.device.manager.CommunicateDeviceManager
import com.example.mtouchpos.service.BluetoothService
import com.example.mtouchpos.service.BluetoothService.Companion.CHARACTERISTIC_WRITE_STRING
import com.example.mtouchpos.viewmodel.usecasemanager.reader.DeviceCommunicateResponseDataImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class CommunicateBluetooth(
    private val context: Context,
    private val mutex: Mutex
): CommunicateDeviceManager {
    private val bluetoothDeviceConnectServiceImpl = MutableStateFlow(BluetoothService())
    private val bluetoothDeviceServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            (binder as BluetoothService.BluetoothServiceBinder).also {
                bluetoothDeviceConnectServiceImpl.value = it.getService()
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    init{ bindingService() }

    override fun bindingService() {
        context.bindService(Intent(context, BluetoothService::class.java), bluetoothDeviceServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun unBindingService() {
        try {
            context.unbindService(bluetoothDeviceServiceConnection)
        } catch (e: Exception) {
        }
    }

    override fun isDeviceServiceInitialized() = bluetoothDeviceConnectServiceImpl.value.run {
        if(this is BluetoothService) {
            isBluetoothGattInitialized()
        } else {
            false
        }
    }

    override fun connect(deviceInfo: String) = flow {
        with(bluetoothDeviceConnectServiceImpl.value) {
            connectDevice(deviceInfo)

            mutex.withLock {
                DeviceCommunicateResponseDataImpl.deviceConnectStatus.collect { emit(it) }
            }
        }
    }

    override fun sendData(byteArray: ByteArray) {
        bluetoothDeviceConnectServiceImpl.value.sendData(byteArray)


//        CoroutineScope(Dispatchers.IO).launch {
//            bluetoothDeviceConnectServiceImpl.collect { bluetoothService ->
//                SpannableStringBuilder().also {
//                    it.append("receive ${byteArray?.size} bytes\n")
//                    it.append(
//                        byteArray?.size?.let {
//                            ResponseSerialCommunicationTransform().toHex(
//                                byteArray,
//                                it
//                            )
//                        }
//                    ).append("\n")
//                    Log.w("requestData", it.toString())
//                }
//                findCharacteristic(bluetoothService.bluetoothGatt!!, BluetoothDeviceConstantObject.CHARACTERISTIC_WRITE_STRING)?.also {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        bluetoothService.bluetoothGatt!!.writeCharacteristic(it, byteArray!!, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
//                    } else {
//                        it.value = byteArray
//                        bluetoothService.bluetoothGatt!!.writeCharacteristic(it)
//                    }
//                }
//            }
//        }
    }
}