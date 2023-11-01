package com.example.data.entity.api.response.tms

data class SummaryTmsResponseData(
    var data: SummaryTmsResponseBody? = null
)

data class SummaryTmsResponseBody(
    var result: String? = null,
    var _idx: String? = null,
    var monthPayCnt: String? = null,
    var dayRef: String? = null,
    var monthPay: String? = null,
    var today: String? = null,
    var dayRefCnt: String? = null,
    var monthRefCnt: String? = null,
    var dayPay: String? = null,
    var dayPayCnt: String? = null,
    var monthRef: String? = null
)
