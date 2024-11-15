package com.example.domain.usecase.device

import com.example.domain.usecase.device.manager.SearchDeviceManager
import com.example.domain.model.device.DeviceInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchBluetoothDevice(private val deviceScan: SearchDeviceManager) {
    suspend fun scan(): Flow<List<DeviceInfo>> = flow {
        deviceScan.scan()
        deviceScan.deviceList.collect { emit(it) }
    }

    fun cancel() {
        deviceScan.cancel()
    }
}