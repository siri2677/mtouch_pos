package com.example.data.entity.api.response.pay

data class ResponseDirectPaymentEntity(
    val result: ResponseDirectPaymentEntityResult,
    val pay: ResponseDirectPaymentEntityPay? = null
)

data class ResponseDirectPaymentEntityResult(
    val resultCd: String,
    val resultMsg: String,
    val advanceMsg: String,
    val create: String
)

data class ResponseDirectPaymentEntityPay(
    val authCd: String? = null,
    val card: ResponseDirectPaymentEntityCard,
    val product: ResponseDirectPaymentEntityProduct,
    val trxId: String,
    val trxType: String,
    val tmnId: String,
    val trackId: String,
    val amount: Int,
    val metadata: ResponseDirectPaymentMetaData? = null
)

data class ResponseDirectPaymentEntityCard(
    val installment: Int,
    val bin: String,
    val last4: String,
    val issuer: String,
    val cardType: String,
    val acquirer: String,
    val issuerCode: String? = null,
    val acquirerCode: String? = null
)

data class ResponseDirectPaymentEntityProduct(
    val name: String,
    val qty: Int,
    val price: Int,
    val desc: String? = null
)

data class ResponseDirectPaymentMetaData(
    val cardAuth: String,
    val authPw: String? = null,
    val authDob: String? = null,
)
