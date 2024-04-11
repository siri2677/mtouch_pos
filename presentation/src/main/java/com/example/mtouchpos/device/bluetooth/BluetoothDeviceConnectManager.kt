package com.example.mtouchpos.device.bluetooth

import android.content.Context
import android.os.Build
import com.example.mtouchpos.device.deviceinterface.DeviceConnectManager
import com.example.domain.vo.DeviceType

class BluetoothDeviceConnectManager(context: Context) : DeviceConnectManager(context) {
    override fun connect(deviceInfo: String) {
        context.stopService(usbDeviceConnectServiceImpl)
        bluetoothDeviceConnectServiceImpl.putExtra(DeviceType.Bluetooth.name, deviceInfo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(bluetoothDeviceConnectServiceImpl)
        } else {
            context.startService(bluetoothDeviceConnectServiceImpl)
        }
    }

    override fun disConnect() {
        context.stopService(bluetoothDeviceConnectServiceImpl)
    }
}