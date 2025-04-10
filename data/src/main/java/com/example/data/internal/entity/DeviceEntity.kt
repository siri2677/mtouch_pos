package com.example.data.internal.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.cardreader.CardReaderData

@Entity(tableName = "device_information")
data class DeviceEntity (
    @PrimaryKey(autoGenerate = true)
    val idx: Int,
    val deviceInfo: String
)