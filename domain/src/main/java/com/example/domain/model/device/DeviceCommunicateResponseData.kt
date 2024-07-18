package com.example.domain.model.device

import kotlinx.coroutines.flow.MutableSharedFlow

interface DeviceCommunicateResponseData {
    val deviceConnectStatus: MutableSharedFlow<DeviceConnectStatus>
    val deviceSerialCommunicate: MutableSharedFlow<ByteArray>
}