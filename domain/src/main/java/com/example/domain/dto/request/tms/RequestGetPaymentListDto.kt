package com.example.domain.dto.request.tms

data class RequestGetPaymentListDto(
    val startDay: String,
    val endDay: String,
    val lastRegTime: String?,
    val lastRegDay: String?
)
