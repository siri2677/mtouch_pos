package com.example.domain.dto.response.pay

import java.util.Date

data class ResponseDirectCancelPaymentDto(
    val result: ResponseDirectPaymentDtoResult,
    val refund: ResponseDirectCancelPaymentDtoRefund? = null
)

data class ResponseDirectCancelPaymentDtoRefund(
    val trxType: String,
    val trackId: String,
    val amount: String,
    val udf1: String? = null,
    val udf2: String? = null,
    val rootTrxId: String,
    val rootTrackId: String? = null,
    val rootTrxDay: String,
    val trxId: String? = null,
    val authCd: String? = null,
    val settle: String? = null,
)

