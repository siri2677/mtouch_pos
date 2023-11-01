package com.example.data.entity.api.request.tms

data class KeyTmsRequestData(
    var data: KeyTmsRequestBody
)

data class KeyTmsRequestBody (
    var tmnId: String,
    var serial: String,
    var appId: String?,
    var mchtId: String,
    var version: String?,
    var telNo: String?
)
