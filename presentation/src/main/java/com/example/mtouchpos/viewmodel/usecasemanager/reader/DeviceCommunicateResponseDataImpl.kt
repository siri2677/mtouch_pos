package com.example.mtouchpos.viewmodel.usecasemanager.reader

import com.example.domain.model.device.DeviceCommunicateResponseData
import com.example.domain.model.device.DeviceConnectStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

object DeviceCommunicateResponseDataImpl: DeviceCommunicateResponseData {
    override val deviceConnectStatus = MutableSharedFlow<DeviceConnectStatus>()
    override val deviceSerialCommunicate = MutableSharedFlow<ByteArray>()
    fun onConnected() {
        CoroutineScope(Dispatchers.IO).launch {
            deviceConnectStatus.emit(DeviceConnectStatus.ConnectComplete)
        }
    }

    fun onDisConnected() {
        CoroutineScope(Dispatchers.IO).launch {
            deviceConnectStatus.emit(DeviceConnectStatus.DisConnected)
        }
    }

    fun onResultCommunicate(byteArray: ByteArray) {
        CoroutineScope(Dispatchers.IO).launch {
            deviceSerialCommunicate.emit(byteArray)
        }
    }
}