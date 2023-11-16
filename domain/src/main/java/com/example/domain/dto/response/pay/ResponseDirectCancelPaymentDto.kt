package com.example.domain.dto.response.pay

import java.util.Date

data class ResponseDirectCancelPaymentDto(
    val result: ResponseDirectPaymentDtoResult,
    val refund: ResponseDirectCancelPaymentDtoRefund?
)

data class ResponseDirectCancelPaymentDtoRefund(
    val trxType: String,
    val trackId: String,
    val amount: String,
    val udf1: String?,
    val udf2: String?,
    val rootTrxId: String,
    val rootTrackId: String?,
    val rootTrxDay: String,
    val trxId: String?,
    val authCd: String?,
    val settle: String?,
)

