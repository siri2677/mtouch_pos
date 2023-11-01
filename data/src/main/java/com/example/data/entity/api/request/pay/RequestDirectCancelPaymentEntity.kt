package com.example.data.entity.api.request.pay

import java.util.Date

data class RequestDirectCancelPaymentEntity(
    var refund: RequestDirectCancelPaymentEntityRefund,
)

data class RequestDirectCancelPaymentEntityRefund(
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
