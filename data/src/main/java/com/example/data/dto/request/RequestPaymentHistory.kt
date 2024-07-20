package com.example.data.dto.request

sealed interface RequestPaymentHistory {
    data object GetSalesHistory : RequestPaymentHistory

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