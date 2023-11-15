package com.example.domain.dto.response.tms

data class ResponseGetPaymentStatisticsDto(
    var amount: String,
    var startDay: String,
    var cnt: String,
    var payCnt: String,
    var rfdAmt: String,
    var payAmt: String,
    var rfdCnt: String,
    var result: String,
    var _idx: String,
    var endDay: String
)
