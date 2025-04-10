package com.example.data.remote.dto.request

sealed interface RequestPaymentHistory {
    data class GetPaymentList(
        val startDay: String,
        val endDay: String,
        val lastRegTime: String?,
        val lastRegDay: String?
    ): RequestPaymentHistory

    data class GetPaymentStatistics(
        val startDay: String,
        val endDay: String
    ): RequestPaymentHistory

    data class DirectPaymentCheck(
        val trxId: String
    ): RequestPaymentHistory
}