package com.example.domain.model.payment

import java.io.Serializable

data class VanData(
    val van: String,
    val vanTrackId: String,
    val vanId: String,
    val dptId: String
): Serializable
