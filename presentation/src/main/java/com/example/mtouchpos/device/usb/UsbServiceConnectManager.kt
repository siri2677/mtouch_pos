package com.example.mtouchpos.device.usb

import android.content.Context
import android.os.Build
import com.example.mtouchpos.device.deviceinterface.DeviceConnectManager
import com.example.domain.vo.DeviceType

class UsbServiceConnectManager(context: Context): DeviceConnectManager(context) {
    override fun connect(deviceInfo: String) {
        context.stopService(bluetoothDeviceConnectServiceImpl)
        usbDeviceConnectServiceImpl.putExtra(DeviceType.Usb.name, deviceInfo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(usbDeviceConnectServiceImpl)
        } else {
            context.startService(usbDeviceConnectServiceImpl)
        }
    }

    override fun disConnect() {
        context.stopService(usbDeviceConnectServiceImpl)
    }
}