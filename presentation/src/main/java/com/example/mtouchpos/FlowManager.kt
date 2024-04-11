package com.example.mtouchpos


import com.example.mtouchpos.vo.DeviceConnectSharedFlow
import com.example.mtouchpos.vo.DeviceList
import com.example.mtouchpos.vo.DeviceSerialCommunicate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class FlowManager {
    companion object {
        val deviceSerialCommunicate: MutableStateFlow<DeviceSerialCommunicate> = MutableStateFlow(
            DeviceSerialCommunicate.Init)
        val deviceConnectSharedFlow: MutableStateFlow<DeviceConnectSharedFlow> = MutableStateFlow(
            DeviceConnectSharedFlow.Init)
        val deviceListSharedFlow: MutableSharedFlow<DeviceList> = MutableSharedFlow(extraBufferCapacity = 10)
    }
}