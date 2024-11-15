package com.example.domain.usecase.device.manager

import com.example.domain.model.device.DeviceConnectStatus
import kotlinx.coroutines.flow.Flow

interface CommunicateDeviceManager {
    fun bindingService()
    fun unBindingService()
    fun connect(deviceInfo: String): Flow<DeviceConnectStatus>
    fun sendData(byteArray: ByteArray)
    fun isDeviceServiceInitialized(): Boolean
}