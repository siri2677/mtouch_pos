package com.example.data.entity.api.response.pay

import com.example.domain.dto.response.pay.ResponseDirectPaymentDtoResult

data class ResponseDirectCancelPaymentEntity(
    val result: ResponseDirectPaymentDtoResult,
    val refund: ResponseDirectCancelPaymentEntityRefund? = null
)

data class ResponseDirectCancelPaymentEntityRefund(
    val payKey: String,
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