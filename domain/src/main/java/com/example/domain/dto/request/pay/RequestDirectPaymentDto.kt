package com.example.domain.dto.request.pay

import java.util.Date

data class RequestDirectPaymentDto(
    val payKey: String,
    val trxType: String = "ONTR",
    val trackId: String = "AXD_" + Date().time,
    val amount: String,
    val payerName: String,
    val payerEmail: String?,
    val payerTel: String,
    val card: RequestDirectPaymentCard,
    val product: RequestDirectPaymentProduct,
    val fillerAmt: Long?,
    val udf1: String?,
    val udf2: String?,
    val metadata: RequestDirectPaymentMetadata,
    val trxId: String?,
    val authCd: String?,
    val settle: String?,
)

data class RequestDirectPaymentProduct(
    val name: String,
    val qty: Int?,
    val price: String?,
    val desc: String?
)

data class RequestDirectPaymentCard(
    val number: String,
    val expiry: String,
    val installment: String,
    val cvv: String?,
    val cardId: String?,
    val last4: String?,
    val issuer: String?,
    val cardType: String?
)

data class RequestDirectPaymentMetadata(
    val cardAuth: String,
    val authPw: String?,
    val authDob: String?
)


