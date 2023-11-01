package com.example.data.entity.api.request.tms

data class RequestCancelPaymentEntity(
    var data: RequestCancelPaymentBody
)

data class RequestCancelPaymentBody(
    var amount: String? = null,
    var installment: String? = null,
    var trxId: String? = null,
)

