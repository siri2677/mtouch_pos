package com.example.domain.model

import com.example.domain.enumclass.DeviceType
import java.io.Serializable

data class DeviceInformation(
    val deviceType: DeviceType,
    val deviceInformation: String
): Serializable
