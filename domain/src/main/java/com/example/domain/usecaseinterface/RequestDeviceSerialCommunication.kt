package com.example.domain.usecaseinterface

import android.hardware.usb.UsbDevice
import androidx.lifecycle.MutableLiveData
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event

interface RequestDeviceSerialCommunication {
    val errorOccur: MutableLiveData<Event<Boolean>>
    val notExistRegisteredDevice: MutableLiveData<Event<String>>
    fun getDeviceType(): DeviceSetting?
    fun setBluetoothDevice(bluetoothAddress: String)
    fun setUsbDevice(usbDevice: UsbDevice)
    fun deleteCurrentRegisteredDeviceType()
    fun getCurrentRegisteredDeviceType(): String
}