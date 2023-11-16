package com.example.domain.dto.request.tms

data class RequestGetUserInformationDto(
    val tmnId: String,
    val serial: String,
    val mchtId: String,
    val appId: String?,
    val version: String?,
    val telNo: String?
)
