package com.example.domain.dto.response.tms

import java.io.Serializable

data class ResponseInsertPaymentDataDTO(
    val result: String,
    val trxId: String,
    var regDay: String?,
    var authCode: String?,
): Serializable

