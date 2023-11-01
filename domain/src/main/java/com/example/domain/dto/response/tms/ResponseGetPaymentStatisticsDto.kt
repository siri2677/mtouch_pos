package com.example.domain.dto.response.tms

data class ResponseGetPaymentStatisticsDto(
    var amount: String? = null,
    var startDay: String? = null,
    var cnt: String? = null,
    var payCnt: String? = null,
    var rfdAmt: String? = null,
    var payAmt: String? = null,
    var rfdCnt: String? = null,
    var result: String? = null,
    var _idx: String? = null,
    var endDay: String? = null
)
