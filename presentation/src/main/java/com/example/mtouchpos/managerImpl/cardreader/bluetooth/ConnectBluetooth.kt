package com.example.mtouchpos.managerImpl.cardreader.bluetooth

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.domain.manager.cardreader.CardReaderConnectManager
import com.example.mtouchpos.service.BluetoothService
import com.example.mtouchpos.service.BluetoothService.Companion.BLUETOOTH_DEVICE
import com.example.mtouchpos.service.UsbService

class ConnectBluetooth(val context: Context) : CardReaderConnectManager {
    private val bluetoothDeviceConnectServiceImpl = Intent(context, BluetoothService::class.java)

    override fun connect(deviceInfo: String) {
        context.stopService(Intent(context, UsbService::class.java))
        with(bluetoothDeviceConnectServiceImpl) {
            putExtra(BLUETOOTH_DEVICE, deviceInfo)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(this)
            } else {
                context.startService(this)
            }
        }
    }

    override fun disConnect() {
        context.stopService(bluetoothDeviceConnectServiceImpl)
    }
}