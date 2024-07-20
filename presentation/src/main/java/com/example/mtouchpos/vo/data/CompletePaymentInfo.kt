package com.example.mtouchpos.vo.data

import com.example.mtouchpos.vo.type.PaymentType
import com.example.mtouchpos.vo.type.TransactionType
import java.io.Serializable

data class CompletePaymentInfo(
    val transactionType: TransactionType,
    val paymentType: PaymentType,
    val amount: String,
    val installment: String,
    val trackId: String,
    val cardNumber: String,
    val issuer: String,
    val authDate: String,
    val authCode: String,
    val trxId: String
): Serializable