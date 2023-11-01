package com.example.data.entity.api.response.tms

data class ResponseCancelPaymentEntity(
    var data: ResponseCancelPaymentBody? = null
)

data class ResponseCancelPaymentBody(
    var result: String? = null,
    var amount: String? = null,
    var van: String? = null,
    var vanId: String? = null,
    var authCd: String? = null,
    var trackId: String? = null,
    var regDay: String? = null,
    var secondKey: String? = null
)
