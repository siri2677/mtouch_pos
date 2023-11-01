package com.example.cleanarchitech_text_0506.deviceinterface

import com.example.cleanarchitech_text_0506.sealed.DeviceConnectSharedFlow
import com.example.cleanarchitech_text_0506.vo.KsnetSocketCommunicationDTO
import com.example.domain.dto.request.tms.RequestInsertPaymentDataDTO
import com.example.domain.dto.request.tms.RequestPaymentDTO
import kotlinx.coroutines.flow.MutableSharedFlow

interface DeviceConnectService {
//    var deviceConnectSharedFlow: MutableSharedFlow<DeviceConnectSharedFlow>
    fun setDeviceConnectSharedFlow(
        deviceConnectSharedFlow: MutableSharedFlow<DeviceConnectSharedFlow>
    ): DeviceConnectService
    fun setEssentialData(
        ksnetSocketCommunicationDTO: KsnetSocketCommunicationDTO,
        deviceConnectSharedFlow: MutableSharedFlow<DeviceConnectSharedFlow>
    ): DeviceConnectService
    fun connectDevice(device: String, byteArray: ByteArray)
    fun sendData(byteArray: ByteArray?)
    fun disConnect()
    fun init()
}