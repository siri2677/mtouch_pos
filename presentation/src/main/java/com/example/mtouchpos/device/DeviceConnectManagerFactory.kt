package com.example.mtouchpos.device

import android.content.Context
import com.example.mtouchpos.device.bluetooth.BluetoothDeviceConnectManager
import com.example.mtouchpos.device.deviceinterface.DeviceConnectManager
import com.example.mtouchpos.device.usb.UsbServiceConnectManager
import com.example.domain.vo.DeviceType

class DeviceConnectManagerFactory(val context: Context) {
    fun getInstance(deviceType: DeviceType): DeviceConnectManager {
        return when(deviceType){
            DeviceType.Bluetooth -> BluetoothDeviceConnectManager(context)
            DeviceType.Usb -> UsbServiceConnectManager(context)
        }
    }
}