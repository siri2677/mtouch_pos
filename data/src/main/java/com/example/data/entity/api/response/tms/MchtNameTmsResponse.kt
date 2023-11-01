package com.example.data.entity.api.response.tms

data class MchtNameTmsResponseData(
    var data: MchtNameTmsResponseBody? = null
)

data class MchtNameTmsResponseBody(
    var idType: String? = null,
    var name: String? = null,
    var mchtId: String? = null
)
