package com.example.cleanarchitech_text_0506.vo

import com.example.cleanarchitech_text_0506.enum.PaymentType
import com.example.cleanarchitech_text_0506.enum.TransactionType
import java.io.Serializable

data class CompletePaymentViewVO(
    val transactionType: TransactionType,
    val paymentType: PaymentType,
    val amount: String = "",
    val installment: String,

    val prodQty: String = "",
    val prodName: String = "",
    val prodPrice: String = "",
    val payerTel: String = "",
    val payerName: String = "",
    val payerEmail: String = "",

    var trackId: String = "",
    var cardNumber: String = "",
    var regDay: String = "",
    var authCode: String = "",
    var trxId: String = ""
): Serializable
