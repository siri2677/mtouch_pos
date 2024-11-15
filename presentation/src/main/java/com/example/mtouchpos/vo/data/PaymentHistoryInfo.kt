package com.example.mtouchpos.vo.data

import com.example.mtouchpos.vo.type.PurchaseType

data class PaymentHistoryInfo(
    val purchaseType: PurchaseType,
    val amount: String,
    val installment: String,
    val trackId: String,
    val authDate: String,
    val authCode: String,
    val trxId: String,
    val cardNumber: String,
    val issuerName: String,
    val rootRegDate: String? = null
)
