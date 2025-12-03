package com.kwonps.domain.model.payment

data class OfflinePaymentPushData(
    val amount: AmountData,
    val installment: Installment,
    val identifiers: ReceiptIdentifiers,
    val approval: PaymentDetailData.ApprovalInfo,
    val cardNumber: String
)

data class ReceiptIdentifiers(
    val vanTrxId: String,
    val rootTrxId: String?,
    val trackId: String?
)
