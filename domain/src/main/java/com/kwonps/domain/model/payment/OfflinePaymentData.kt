package com.kwonps.domain.model.payment

sealed class OfflinePaymentData {
    abstract val amountData: AmountData
    abstract val installment: Installment
    abstract val trackId: String?

    data class Approve(
        override val amountData: AmountData,
        override val installment: Installment,
        override val trackId: String? = null
    ) : OfflinePaymentData()

    data class Cancel(
        override val amountData: AmountData,
        override val installment: Installment,
        override val trackId: String? = null,
        val rootTrxId: String?,
        val authCode: String,
        val authDate: String
    ) : OfflinePaymentData()
}
