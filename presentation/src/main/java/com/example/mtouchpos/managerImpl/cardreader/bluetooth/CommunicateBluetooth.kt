package com.example.mtouchpos.managerImpl.cardreader.bluetooth

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.domain.manager.cardreader.CardReaderCommunicateManager
import com.example.domain.model.cardreader.CardReaderStatus
import com.example.mtouchpos.managerImpl.cardreader.CardReaderResponseImpl
import com.example.mtouchpos.service.BluetoothService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class CommunicateBluetooth(
    private val context: Context,
    private val mutex: Mutex
): CardReaderCommunicateManager {
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

    override fun stopRetry(byteArray: ByteArray) {
        bluetoothDeviceConnectServiceImpl.value.stopRetry(byteArray)
    }

    override fun connect(deviceInfo: String) {
//        return flow {
//            CardReaderResponseImpl.deviceConnectStatus.collect { emit(it) }
//        }
        bluetoothDeviceConnectServiceImpl.value.connectDevice(deviceInfo)
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