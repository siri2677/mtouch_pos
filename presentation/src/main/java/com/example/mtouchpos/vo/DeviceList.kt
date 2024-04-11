package com.example.mtouchpos.vo

import android.bluetooth.BluetoothDevice
import android.hardware.usb.UsbDevice

sealed interface DeviceList {
    object Init: DeviceList
    data class BluetoothList(val devices: ArrayList<BluetoothDevice>): DeviceList
    data class UsbList(val devices: ArrayList<UsbDevice>): DeviceList
}