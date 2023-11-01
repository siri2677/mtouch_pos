package com.example.domain.dto.response.tms

data class ResponsePaymentDTO(
    var result: String? = null,
    var van: String? = null,
    var vanId: String? = null,
    var trackId: String? = null,
    var secondKey: String? = null
)