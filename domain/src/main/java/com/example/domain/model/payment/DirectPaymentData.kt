package com.example.domain.model.payment

import java.util.Date

sealed interface DirectPaymentData {
    data class Approve(
        val totalAmount: Int,
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
    ): DirectPaymentData

    data class Cancel(
        val totalAmount: Int,
        val installment: String,
        val cardNumber: String,
        val trackId: String,
        val rootTrxId: String
    ): DirectPaymentData
}
