package com.example.domain.dto.request.tms

data class RequestGetPaymentListDto(
    var startDay: String,
    var endDay: String,
    var lastRegTime: String? = null,
    var lastRegDay: String? = null
)
