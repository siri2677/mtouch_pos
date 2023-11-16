package com.example.domain.dto.request.tms

import com.example.domain.dto.PaymentDTO

data class RequestCancelPaymentDTO(
    val amount: String,
    val installment: String,
    val trxId: String,
    val token: String
): PaymentDTO

