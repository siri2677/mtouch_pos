package com.example.domain.usecase.device

import com.example.domain.usecase.device.manager.ConnectDeviceManager
import com.example.domain.model.device.DeviceCommunicateResponseData
import com.example.domain.model.device.DeviceConnectStatus
import com.example.domain.model.device.DeviceInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Provider

class ConnectCardReader(private val deviceOperationCallback: DeviceCommunicateResponseData) {
    private val mutex = Mutex()

    suspend operator fun invoke(
        deviceInfo: DeviceInfo,
        deviceConnectManager: ConnectDeviceManager
    ): Flow<DeviceConnectStatus> = flow {
        deviceConnectManager.connect(deviceInfo.deviceInformation)

        mutex.withLock {
            deviceOperationCallback.deviceConnectStatus.collect { emit(it) }
        }
    }
}