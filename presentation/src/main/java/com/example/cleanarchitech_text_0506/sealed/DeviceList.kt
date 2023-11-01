package com.example.cleanarchitech_text_0506.sealed

import android.bluetooth.BluetoothDevice
import android.hardware.usb.UsbDevice

sealed class DeviceList {
    data class BluetoothList(val devices: ArrayList<BluetoothDevice>): DeviceList()
    data class USBList(val devices: ArrayList<UsbDevice>): DeviceList()
}