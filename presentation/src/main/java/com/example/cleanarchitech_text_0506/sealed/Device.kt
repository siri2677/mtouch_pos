package com.example.cleanarchitech_text_0506.sealed

import android.bluetooth.BluetoothDevice
import android.hardware.usb.UsbDevice

sealed class Device {
    data class Bluetooth(val device: BluetoothDevice): Device()
    data class USB(val device: UsbDevice): Device()
}
