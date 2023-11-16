package com.example.domain.dto.response.tms

data class ResponsePaymentDTO(
    val result: String,
    val van: String,
    val vanId: String,
    val trackId: String,
    val secondKey: String
)