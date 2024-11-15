package com.example.domain.model.payment

import java.io.Serializable

sealed class OfflinePaymentData: Serializable {
    abstract val amountData: AmountData
    abstract val installment: String
    abstract val trackId: String?

    data class Approve(
        override val amountData: AmountData,
        override val installment: String,
        override val trackId: String? = null
    ) : OfflinePaymentData()

    data class Cancel(
        override val amountData: AmountData,
        override val installment: String,
        override val trackId: String? = null,
        val rootTrxId: String,
        val authCode: String,
        val authDate: String
    ) : OfflinePaymentData()
}