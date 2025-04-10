package com.example.domain.model.paymentHistory

data class PaymentHistoryData (
    val rfdTime: String,
    val amount: String,
    val van: String,
    val vanTrxId: String,
    val authCd: String,
    val tmnId: String,
    val trackId: String,
    val bin: String,
    val cardType: String,
    val trxId: String,
    val issuer: String,
    val regDay: String,
    val resultMsg: String,
    val number: String,
    val trxResult: String,
    val regTime: String,
    val vanId: String,
    val _idx: String,
    val installment: String,
    val rfdDay: String,
    val mchtId: String,
    val brand: String,
    val rfdId: String
)