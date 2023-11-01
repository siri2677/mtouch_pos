package com.example.domain.dto.request.tms

data class RequestCancelPaymentDTO(
    var amount: String? = null,
    var installment: String? = null,
    var trxId: String? = null,
    var token: String
)

