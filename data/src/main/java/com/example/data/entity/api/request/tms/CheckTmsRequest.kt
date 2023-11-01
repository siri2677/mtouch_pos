package com.example.data.entity.api.request.tms

data class CheckTmsRequestData(
    var data: CheckTmsRequestBody
)

data class CheckTmsRequestBody(
    var trxId: String? = null
)
