package com.example.mtouchpos.vo.info

import com.example.domain.model.payment.VanData
import java.io.Serializable


sealed interface PaymentProcessState: Serializable {
    object Init: PaymentProcessState
    object Loading: PaymentProcessState
    data class Error(val message: String): PaymentProcessState
    data class Approve(val vanTrackId: String): PaymentProcessState
    data class Complete(
        val data: ApprovedPaymentType.CompletePaymentViewInfo
    ): PaymentProcessState
    sealed interface CommunicateCardReader: PaymentProcessState {
        object Loading: CommunicateCardReader
        object Active: CommunicateCardReader
        object Inactive: CommunicateCardReader
        data class Connecting(val retryCount: Int): CommunicateCardReader
        data class Error(val message: String): CommunicateCardReader
        object InsertIC: CommunicateCardReader
        object ReadingIC: CommunicateCardReader
        data class Fallback(val message: String): CommunicateCardReader
    }
}