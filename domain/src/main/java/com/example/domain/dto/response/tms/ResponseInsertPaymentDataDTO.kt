package com.example.domain.dto.response.tms

import java.io.Serializable

data class ResponseInsertPaymentDataDTO(
    val result: String,
    val trxId: String,
    val installment: String? = null,
    val trackId: String? = null,
    val cardNumber: String? = null,
    val amount: String? = null,
    var regDay: String? = null,
    var authCode: String? = null,
): Serializable

