package com.example.domain.usecaseinterface.bluetooth

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event

interface BluetoothDeviceConnectUseCaseImpl {
//    val isConnectDevice: MutableLiveData<Event<Boolean>>
    fun bluetoothDeviceConnect(bluetoothDevice: BluetoothDevice)
    fun bluetoothDeviceDisConnect()
//    fun bluetoothDeviceUnBinding()
}