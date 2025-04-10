package com.example.data.internal.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_information")
data class UserEntity(
    @PrimaryKey
    val tmnId: String,
    val serial: String,
    val mchtId: String,
    val mchtName: String,
    val telNo: String,
    val ceoName: String,
    val address: String,
    val identity: String,
    val semiAuth: String,
    val appDirect: String,
    val key: String,
    val vat: String,
    val apiMaxInstall: String,
    val payKey: String
)