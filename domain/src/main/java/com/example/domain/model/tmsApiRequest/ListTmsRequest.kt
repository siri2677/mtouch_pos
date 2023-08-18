package com.mtouch.domain.model.tmsApiRequest

data class ListTmsRequestData(
    var data: ListTmsRequestBody
)

data class ListTmsRequestBody(
    var startDay: String? = null,
    var endDay: String? = null,
    var lastRegTime: String? = null,
    var lastRegDay: String? = null
)
