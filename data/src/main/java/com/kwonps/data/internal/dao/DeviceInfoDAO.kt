package com.kwonps.data.internal.dao

import androidx.room.Dao
import androidx.room.Query
import com.kwonps.data.internal.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceInfoDAO {
    @Query("SELECT * FROM device_information WHERE idx = 0")
    fun get(): Flow<DeviceEntity>

    @Query("SELECT * FROM device_information WHERE idx = 0")
    fun getCurrentRegisteredInfo(): DeviceEntity

    @Query("UPDATE device_information SET deviceInfo = :deviceInfo WHERE idx = 0")
    fun updateDeviceInfo(deviceInfo: String)
}