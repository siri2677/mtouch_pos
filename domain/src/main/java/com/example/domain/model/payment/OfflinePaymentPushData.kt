package com.example.domain.model.payment

data class OfflinePaymentPushData(
    val amount: String,
    val installment: String,
    val vanTrxId: String,
    val rootTrxId: String?,
    val trackId: String?,
    val authCode : String,
    val authDate: String,
    val cardNumber: String
)