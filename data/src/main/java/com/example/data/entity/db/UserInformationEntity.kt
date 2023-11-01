package com.example.data.entity.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_information")
data class UserInformationEntity(
    @PrimaryKey
    var tmnId: String,
    var serial: String,
    var appId: String?,
    var mchtId: String,
    var version: String?,
    var telNo: String?
)