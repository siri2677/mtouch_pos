package com.example.domain.usecaseinterface

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event

interface DeviceSetting {
    val isFirstConnectComplete: MutableLiveData<Event<Boolean>>
    val permissionCheckComplete: MutableLiveData<Event<Boolean>>
    fun deviceConnect(DeviceInformation: String)
    fun deviceDisConnect()
    fun bindingService()
    fun unBindingService()
//    fun deviceResister(bluetoothDevice: BluetoothDevice)
//    fun deviceUnResister()
    fun requestDeviceSerialCommunication(byteArray: ByteArray)
}