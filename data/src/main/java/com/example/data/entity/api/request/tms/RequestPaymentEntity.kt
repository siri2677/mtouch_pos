package com.example.data.entity.api.request.tms

data class RequestPaymentEntity (
    var data: RequestPaymentBody
)

data class RequestPaymentBody (
    var amount: String? = null,
    var installment: String? = null
)

