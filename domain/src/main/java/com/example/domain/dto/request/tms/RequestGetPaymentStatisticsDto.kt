package com.example.domain.dto.request.tms

data class RequestGetPaymentStatisticsDto (
    var startDay: String? = null,
    var endDay: String? = null
)