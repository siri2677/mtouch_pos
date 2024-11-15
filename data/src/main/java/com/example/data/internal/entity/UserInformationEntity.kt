package com.example.data.internal.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_information")
data class UserInformationEntity(
    @PrimaryKey
    val tmnId: String,
    val serial: String,
    val mchtId: String,
    val semiAuth: String,
    val appDirect: String,
    val key: String,
    val vat: String,
    val apiMaxInstall: String,
    val payKey: String
)