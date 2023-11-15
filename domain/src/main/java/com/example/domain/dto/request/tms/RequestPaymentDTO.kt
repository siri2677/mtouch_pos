package com.example.domain.dto.request.tms

import com.example.domain.dto.PaymentDTO

data class RequestPaymentDTO (
    val amount: String? = null,
    val installment: String? = null,
    val token: String
)

