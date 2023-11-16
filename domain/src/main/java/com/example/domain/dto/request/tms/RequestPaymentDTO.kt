package com.example.domain.dto.request.tms

import com.example.domain.dto.PaymentDTO

data class RequestPaymentDTO (
    val amount: String,
    val installment: String,
    val token: String
)

