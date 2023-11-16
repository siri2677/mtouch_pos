package com.example.domain.dto.response.tms

data class ResponseGetSummaryPaymentStatisticsDto(
    var result: String,
    var _idx: String,
    var today: String,
    var dayRef: String,
    var dayRefCnt: String,
    var dayPay: String,
    var dayPayCnt: String,
    var monthPay: String,
    var monthPayCnt: String,
    var monthRefCnt: String,
    var monthRef: String
)
