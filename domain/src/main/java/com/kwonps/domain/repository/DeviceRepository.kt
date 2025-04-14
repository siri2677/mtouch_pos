package com.kwonps.domain.repository

import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun getDeviceInfo(): Flow<String>
    fun getCurrentRegisteredDeviceInfo(): String
    fun setDeviceInfo(deviceInformation: String)
}