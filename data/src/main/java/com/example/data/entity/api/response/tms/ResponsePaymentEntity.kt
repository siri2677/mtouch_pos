package com.example.data.entity.api.response.tms

data class ResponsePaymentEntity(
    var data: ResponsePaymentBody? = null
)

data class ResponsePaymentBody(
    var result: String? = null,
    var van: String? = null,
    var vanId: String? = null,
    var trackId: String? = null,
    var secondKey: String? = null
)