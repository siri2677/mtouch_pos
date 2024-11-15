package com.example.mtouchpos.viewmodel.factory

import android.content.Context
import com.example.domain.usecase.device.manager.ConnectDeviceManager
import com.example.mtouchpos.viewmodel.DeviceSettingViewModel
import com.example.mtouchpos.viewmodel.usecasemanager.reader.bluetooth.ConnectBluetooth
import com.example.mtouchpos.viewmodel.usecasemanager.reader.usb.ConnectUsb

class CardReaderFactory(private val context: Context) {
    fun getConnectManger(deviceType: DeviceSettingViewModel.DeviceType): ConnectDeviceManager =
        when(deviceType) {
            DeviceSettingViewModel.DeviceType.Bluetooth -> {
                ConnectBluetooth(context)
            }
            DeviceSettingViewModel.DeviceType.Usb -> {
                ConnectUsb(context)
            }
        }
}