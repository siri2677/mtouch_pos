package com.example.domain.usecaseinterface.usb

import android.bluetooth.BluetoothDevice
import android.hardware.usb.UsbDevice
import androidx.lifecycle.MutableLiveData
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event

interface UsbDeviceConnectUsecaseImpl {
    val permissionCheckComplete : MutableLiveData<Event<Boolean>>
    fun usbDeviceConnect()
    fun usbDeviceDisConnect()

}