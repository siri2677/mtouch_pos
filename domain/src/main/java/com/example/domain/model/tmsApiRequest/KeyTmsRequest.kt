package com.mtouch.domain.model.tmsApiRequest

data class KeyTmsRequestData(
    var data: KeyTmsRequestBody,
)

data class KeyTmsRequestBody(
    var tmnId: String? = null,
    var serial: String? = null,
    var appId: String? = null,
    var mchtId: String? = null,
    var version: String? = null,
    var telNo: String? = null
)
