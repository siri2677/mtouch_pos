package com.example.domain.dto.request.pay

import java.util.Date

data class RequestDirectCancelPaymentDto(
    val payKey: String,
    val trxType: String = "ONTR",
    val trackId: String = "AXD_" + Date().time,
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