package com.example.data.entity.api.response.tms


data class CheckTmsResponseData(
    var data: CheckTmsResponseBody? = null
)

data class CheckTmsResponseBody(
    var trxId: String? = null
)
