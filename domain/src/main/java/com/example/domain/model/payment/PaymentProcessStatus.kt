package com.example.domain.model.payment

import java.io.Serializable

sealed interface PaymentProcessStatus: Serializable {
    data object Init: PaymentProcessStatus

    data class ApprovePayment(val trackId: String): PaymentProcessStatus

    sealed class DeviceCommunication: PaymentProcessStatus {
        data object InsertIC : DeviceCommunication()
        data object ReadingIC : DeviceCommunication()
        data class FallBack(
            val description: String,
            val code: String
        ) : DeviceCommunication()
    }

    data class CompleteDeviceCommunication(
        val trackII: ByteArray,
        val readerModelNum: ByteArray,
        val encryptInfo: ByteArray,
        val reqEMVData: ByteArray,
        val cardNumber: String
    ): PaymentProcessStatus

    data class CompletePayment(val data: PaymentDetailData): PaymentProcessStatus
}