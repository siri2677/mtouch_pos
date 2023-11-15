package com.example.cleanarchitech_text_0506.sealed

import com.example.cleanarchitech_text_0506.vo.CompletePaymentViewVO
import com.example.cleanarchitech_text_0506.vo.KsnetSocketCommunicationDTO
import com.example.data.entity.api.response.tms.ResponseInsertPaymentDataEntity
import com.example.domain.dto.request.tms.RequestInsertPaymentDataDTO
import com.example.domain.dto.response.tms.ResponseInsertPaymentDataDTO
import java.io.Serializable

sealed interface DeviceConnectSharedFlow: Serializable {
    data class DeviceListFlow(val flow: DeviceList): DeviceConnectSharedFlow
    data class PaymentCompleteFlow(val completePaymentViewVO: CompletePaymentViewVO): DeviceConnectSharedFlow
    data class ConnectCompleteFlow(val flow: Boolean): DeviceConnectSharedFlow
    data class PermissionCheckCompleteFlow(val flow: Boolean): DeviceConnectSharedFlow
    data class RequestSocketCommunication(val ksnetSocketCommunicationDTO: KsnetSocketCommunicationDTO): DeviceConnectSharedFlow
    class SerialCommunicationMessageFlow(var message: String): DeviceConnectSharedFlow {
        private lateinit var data: String
        fun getData(): String = try {
            data
        } catch (e: Exception){
            ""
        }
        fun setData(data: String) { this.data = data }
    }
}