package com.example.domain.repository

import com.example.domain.model.cardreader.CardReaderData
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun getDeviceInfo(): Flow<String>
    fun getCurrentRegisteredDeviceInfo(): String
    fun setDeviceInfo(deviceInformation: String)
}