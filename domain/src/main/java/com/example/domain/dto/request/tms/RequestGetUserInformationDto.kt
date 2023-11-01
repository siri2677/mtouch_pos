package com.example.domain.dto.request.tms

data class RequestGetUserInformationDto(
    var tmnId: String? = null,
    var serial: String? = null,
    var appId: String? = null,
    var mchtId: String? = null,
    var version: String? = null,
    var telNo: String? = null
)
