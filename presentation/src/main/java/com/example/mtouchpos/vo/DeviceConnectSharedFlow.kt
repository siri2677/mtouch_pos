package com.example.mtouchpos.vo

import java.io.Serializable

sealed interface DeviceConnectSharedFlow: Serializable {
    object Init: DeviceConnectSharedFlow
    data class ConnectCompleteFlow(val device: DeviceContentsVO): DeviceConnectSharedFlow
    data class IsDisConnected(val boolean: Boolean): DeviceConnectSharedFlow
}