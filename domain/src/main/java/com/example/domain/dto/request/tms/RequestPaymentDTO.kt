package com.example.domain.dto.request.tms

import com.example.domain.dto.PaymentDTO

data class RequestPaymentDTO (
    var amount: String? = null,
    var installment: String? = null,
    var prodQty: String? = null,
    var prodName: String? = null,
    var prodPrice: String? = null,
    var payerTel: String? = null,
    var payerName: String? = null,
    var payerEmail: String? = null,
    var token: String
): PaymentDTO

