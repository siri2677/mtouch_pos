package com.example.mtouchpos.device.deviceinterface

import android.content.Context
import android.content.Intent
import com.example.mtouchpos.device.bluetooth.BluetoothDeviceConnectService
import com.example.mtouchpos.device.usb.UsbDeviceConnectService

abstract class DeviceConnectManager(val context: Context) {
    val usbDeviceConnectServiceImpl = Intent(context, UsbDeviceConnectService::class.java)
    val bluetoothDeviceConnectServiceImpl = Intent(context, BluetoothDeviceConnectService::class.java)
    open fun connect(deviceInfo: String) {}
    open fun disConnect() {}
}