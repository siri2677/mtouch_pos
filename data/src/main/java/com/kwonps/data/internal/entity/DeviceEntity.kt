package com.kwonps.data.internal.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_information")
data class DeviceEntity (
    @PrimaryKey(autoGenerate = true)
    val idx: Int,
    val deviceInfo: String
)