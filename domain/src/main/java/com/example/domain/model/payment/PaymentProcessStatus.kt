package com.example.domain.model.payment


sealed interface PaymentProcessStatus {
    data object Init: PaymentProcessStatus

    sealed class ConnectReader: PaymentProcessStatus {
        data class Retry(val count: Int) : ConnectReader()
        data object Fail : ConnectReader()
    }

    data class ApprovePayment(val trackId: String): PaymentProcessStatus

    sealed class CommunicateReader: PaymentProcessStatus {
        data object InsertIC : CommunicateReader()
        data object ReadingIC : CommunicateReader()
        data class FallBack(
            val description: String,
            val code: String
        ) : CommunicateReader()
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