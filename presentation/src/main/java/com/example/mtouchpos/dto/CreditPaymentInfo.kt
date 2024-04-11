package com.example.mtouchpos.dto

data class CreditPaymentInfo (
    val amount: String,
    val authCd: String,
    val trackId: String,
    val bin: String,
    val cardType: String,
    val trxId: String,
    val regDay: String,
    val number: String,
    val trxResult: String,
    val regTime: String,
    val installment: String,
    val brand: String,
    val rfdTime: String?,
    val rfdDay: String?
)