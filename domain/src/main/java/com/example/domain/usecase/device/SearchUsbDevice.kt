package com.example.domain.usecase.device

import com.example.domain.usecase.device.manager.SearchDeviceManager
import com.example.domain.model.device.DeviceInfo

class SearchUsbDevice(private val deviceScan: SearchDeviceManager) {
    operator fun invoke(): List<DeviceInfo> = deviceScan.run { scan(); deviceList.value }
}