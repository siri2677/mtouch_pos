package com.example.domain.dto.request.tms

import com.example.domain.dto.PaymentDTO

data class RequestCancelPaymentDTO(
    var amount: String? = null,
    var installment: String? = null,
    var trxId: String? = null,
    var token: String
): PaymentDTO

