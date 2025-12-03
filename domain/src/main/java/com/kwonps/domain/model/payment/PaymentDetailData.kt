package com.kwonps.domain.model.payment

data class PaymentDetailData(
    val amount: AmountData,
    val installment: Installment,
    val approval: ApprovalInfo,
    val tracking: TrackingInfo,
    val card: CardInfo,
    val remainAmount: String? = null
) {
    data class ApprovalInfo(
        val authCode: String,
        val authDate: String,
        val rootRegDate: String? = null
    )

    data class TrackingInfo(
        val trackId: String?,
        val trxId: String?,
        val trxResult: String
    )

    data class CardInfo(
        val cardNumber: String,
        val cardType: String?,
        val issuerName: String? = null,
        val purchaseName: String? = null
    )
}
