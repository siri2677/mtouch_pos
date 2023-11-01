package com.example.data.entity.api.request.tms

data class MchtNameTmsRequestData(
    var data: MchtNameTmsRequestBody
)

data class MchtNameTmsRequestBody(
    var mchtId: String? = null
)
