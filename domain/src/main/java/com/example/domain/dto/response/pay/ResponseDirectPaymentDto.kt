package com.example.domain.dto.response.pay

data class ResponseDirectPaymentDto(
    val result: ResponseDirectPaymentDtoResult,
    val pay: ResponseDirectPaymentDtoPay? = null
)

data class ResponseDirectPaymentDtoResult(
    val resultCd: String,
    val resultMsg: String,
    val advanceMsg: String,
    val create: String
)

data class ResponseDirectPaymentDtoPay(
    val authCd: String? = null,
    val card: ResponseDirectPaymentDtoCard,
    val product: ResponseDirectPaymentDtoProduct,
    val trxId: String,
    val trxType: String,
    val tmnId: String,
    val trackId: String,
    val amount: Int,
    val metadata: ResponseDirectPaymentMetaData? = null
)

data class ResponseDirectPaymentDtoCard(
    val installment: Int,
    val bin: String,
    val last4: String,
    val issuer: String,
    val cardType: String,
    val acquirer: String,
    val issuerCode: String? = null,
    val acquirerCode: String? = null
)

data class ResponseDirectPaymentDtoProduct(
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