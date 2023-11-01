package com.example.domain.dto.response.tms


data class ResponseCancelPaymentDTO(
    var result: String? = null,
    var amount: String? = null,
    var van: String? = null,
    var vanId: String? = null,
    var authCd: String? = null,
    var trackId: String? = null,
    var regDay: String? = null,
    var secondKey: String? = null
)
