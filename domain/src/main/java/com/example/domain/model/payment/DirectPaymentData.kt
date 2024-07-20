package com.example.domain.model.payment

data class DirectPaymentData(
    val amount: Int,
    val installment: String,
    val trackId: String,
    val cardNumber: String,
    val expiry: String,
    val cardAuth: String,
    val productName: String,
    val payerName: String,
    val payerTel: String,
    val authPw: String? = null,
    val authDob: String? = null
)