package com.example.cleanarchitech_text_0506.sealed

import android.util.Log

sealed interface DeviceConnectSharedFlow {
    data class DeviceListFlow(val flow: DeviceList): DeviceConnectSharedFlow
    data class PaymentCompleteFlow(val flow: Boolean): DeviceConnectSharedFlow
    data class ConnectCompleteFlow(val flow: Boolean): DeviceConnectSharedFlow
    data class PermissionCheckCompleteFlow(val flow: Boolean): DeviceConnectSharedFlow
    data class RequestSocketCommunication(val byteArray: ByteArray): DeviceConnectSharedFlow
    class SerialCommunicationMessageFlow(var flow: String): DeviceConnectSharedFlow {
        private lateinit var data: String
        fun getData(): String = try {
            data
        } catch (e: Exception){
            ""
        }
        fun setData(data: String) { this.data = data }
    }
}