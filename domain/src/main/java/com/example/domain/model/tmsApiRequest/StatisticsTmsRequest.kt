package com.mtouch.domain.model.tmsApiRequest

data class StatisticsTmsRequestData (
    var data: StatisticsTmsRequestBody
)

data class StatisticsTmsRequestBody (
    var startDay: String? = null,
    var endDay: String? = null
)