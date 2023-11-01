package com.example.cleanarchitech_text_0506.vo

import com.example.cleanarchitech_text_0506.enum.PaymentType
import java.io.Serializable

data class CompletePaymentViewVO(
    val paymentType: PaymentType,
    val trackId: String,
    val cardNumber: String,
    val amount: String,
    val regDay: String,
    val authCode: String,
    val trxId: String
): Serializable
