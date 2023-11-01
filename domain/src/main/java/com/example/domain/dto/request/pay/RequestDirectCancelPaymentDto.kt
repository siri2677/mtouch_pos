package com.example.domain.dto.request.pay

import java.util.Date

data class RequestDirectCancelPaymentDto(
    val payKey: String,
    val trxType: String = "ONTR",
    val trackId: String = "AXD_" + Date().time,
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