package com.example.domain.model.device

import java.io.Serializable

sealed interface DeviceConnectStatus: Serializable {
    data object ConnectComplete: DeviceConnectStatus
    data object DisConnected: DeviceConnectStatus
}