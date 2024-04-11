package com.example.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_information")
data class UserInformationEntity(
    @PrimaryKey
    val tmnId: String,
    val serial: String,
    val mchtId: String,
    val appId: String?,
    val version: String?,
    val telNo: String?
)