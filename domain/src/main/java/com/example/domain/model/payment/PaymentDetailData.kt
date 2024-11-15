package com.example.domain.model.payment

data class PaymentDetailData(
    val amount: String,
    val installment: String,
    val authCode : String,
    val authDate: String,
    val trackId: String,
    val trxId: String,
    val trxResult: String,
    val cardNumber: String,
    val issuerName: String? = null,
    val rootRegDate: String? = null
)


