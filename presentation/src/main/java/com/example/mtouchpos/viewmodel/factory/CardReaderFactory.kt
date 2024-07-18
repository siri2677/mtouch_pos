package com.example.mtouchpos.viewmodel.factory

import android.content.Context
import com.example.domain.usecase.device.manager.ConnectDeviceManager
import com.example.mtouchpos.viewmodel.usecasemanager.reader.bluetooth.ConnectBluetooth
import com.example.mtouchpos.viewmodel.usecasemanager.reader.usb.ConnectUsb
import com.example.mtouchpos.vo.type.DeviceType

class CardReaderFactory(private val context: Context) {
    fun getConnectManger(deviceType: DeviceType): ConnectDeviceManager =
        when(deviceType) {
            DeviceType.Bluetooth -> {
                ConnectBluetooth(context)
            }
            DeviceType.Usb -> {
                ConnectUsb(context)
            }
        }
}