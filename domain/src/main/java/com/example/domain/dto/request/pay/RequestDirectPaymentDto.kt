package com.example.domain.dto.request.pay

import java.util.Date

data class RequestDirectPaymentDto(
    var payKey: String,
    var trxType: String = "ONTR",
    var trackId: String = "AXD_" + Date().time,
    var amount: String,
    var payerName: String,
    var payerEmail: String? = null,
    var payerTel: String,
    var card: RequestDirectPaymentCard,
    var product: RequestDirectPaymentProduct,
    var fillerAmt: Long? = null,
    var udf1: String? = null,
    var udf2: String? = null,
    var metadata: RequestDirectPaymentMetadata? = null,
    var trxId: String? = null,
    var authCd: String? = null,
    var settle: String? = null,
)

data class RequestDirectPaymentProduct(
    var name: String,
    var qty: Int? = null,
    var price: String? = null,
    var desc: String? = null
)

data class RequestDirectPaymentCard(
    var number: String,
    var expiry: String,
    var installment: String,
    var cvv: String? = null,
    var cardId: String? = null,
    var last4: String? = null,
    var issuer: String? = null,
    var cardType: String? = null,
)

data class RequestDirectPaymentMetadata(
    var cardAuth: String,
    var authPw: String? = null,
    var authDob: String? = null
)


