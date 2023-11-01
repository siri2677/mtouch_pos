package com.example.data.entity.api.request.pay

data class RequestDirectPaymentEntity(
    var pay: RequestDirectPaymentPay
)

data class RequestDirectPaymentPay(
    var trxType: String,
    var trackId: String,
    var amount: String,
    var payerName: String,
    var payerEmail: String? = null,
    var payerTel: String,
    var card: Card,
    var product: Product,
    var fillerAmt: Long? = null,
    var udf1: String? = null,
    var udf2: String? = null,
    var metadata: Metadata? = null,
    var trxId: String? = null,
    var authCd: String? = null,
    var settle: String? = null,
)

data class Product(
    var name: String,
    var qty: Int? = null,
    var price: String? = null,
    var desc: String? = null
)

data class Card(
    var number: String,
    var expiry: String,
    var installment: String,
    var cvv: String? = null,
    var cardId: String? = null,
    var last4: String? = null,
    var issuer: String? = null,
    var cardType: String? = null,
)

data class Metadata(
    var cardAuth: String,
    var authPw: String? = null,
    var authDob: String? = null
)