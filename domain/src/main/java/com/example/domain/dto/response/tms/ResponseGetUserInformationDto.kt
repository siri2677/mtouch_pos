package com.example.domain.dto.response.tms

data class ResponseGetUserInformationDto(
    val tmnId: String,
    val bankName: String,
    val phoneNo: String,
    val result: String,
    val Authorization: String,
    val semiAuth: String,
    val identity: String,
    val accntHolder: String,
    val appDirect: String,
    val ceoName: String,
    val addr: String,
    val key: String,
    val agencyEmail: String,
    val distEmail: String,
    val vat: String,
    val agencyTel: String,
    val agencyName: String,
    val telNo: String,
    val apiMaxInstall: String,
    val distTel: String,
    val name: String,
    val distName: String,
    val payKey: String,
    val account: String
)
