package com.example.domain.usecase.device.manager

import com.example.domain.model.device.DeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow

interface SearchDeviceManager {
    val deviceList: MutableStateFlow<List<DeviceInfo>>
    fun scan()
    fun cancel()
}