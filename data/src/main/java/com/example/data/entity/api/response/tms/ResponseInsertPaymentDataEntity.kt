package com.example.data.entity.api.response.tms

data class ResponseInsertPaymentDataEntity(
    var data: ResponseInsertPaymentDataBody? = null
)

data class ResponseInsertPaymentDataBody(
    var result: String? = null,
    var trxId: String? = null
)

