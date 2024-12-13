package com.example.mtouchpos.managerImpl.cardreader.usb

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.domain.manager.cardreader.CardReaderConnectManager
import com.example.mtouchpos.service.BluetoothService
import com.example.mtouchpos.service.UsbService
import com.example.mtouchpos.service.UsbService.Companion.USB_DEVICE

class ConnectUsb(val context: Context): CardReaderConnectManager {
    private val usbDeviceConnectServiceImpl = Intent(context, UsbService::class.java)

    override fun connect(deviceInfo: String) {
        context.stopService(Intent(context, BluetoothService::class.java))
        with(usbDeviceConnectServiceImpl){
            putExtra(USB_DEVICE, deviceInfo)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(this)
            } else {
                context.startService(this)
            }
        }
    }

    override fun disConnect() {
        context.stopService(usbDeviceConnectServiceImpl)
    }
}