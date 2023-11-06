package com.example.cleanarchitech_text_0506.vo

import com.example.cleanarchitech_text_0506.enum.PaymentType
import com.example.cleanarchitech_text_0506.enum.TransactionType
import java.io.Serializable

data class CompletePaymentViewVO(
    val transactionType: TransactionType,
    val paymentType: PaymentType,
    val installment: String,
    val trackId: String,
    val cardNumber: String,
    val amount: String,
    val regDay: String,
    val authCode: String,
    val trxId: String
): Serializable
