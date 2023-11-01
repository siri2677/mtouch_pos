package com.example.data.entity.api.request.tms

data class StatisticsTmsRequestData (
    var data: StatisticsTmsRequestBody
)

data class StatisticsTmsRequestBody (
    var startDay: String? = null,
    var endDay: String? = null
)