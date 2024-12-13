package com.example.mtouchpos.vo.info

import com.example.mtouchpos.viewmodel.OfflinePaymentVM
import com.example.mtouchpos.vo.type.PurchaseType
import java.io.Serializable

sealed class ApprovedPaymentType {
    abstract val purchaseType: PurchaseType
    abstract val amount: String
    abstract val installment: String
    abstract val trackId: String
    abstract val authDate: String
    abstract val authCode: String
    abstract val trxId: String
    abstract val cardNumber: String

    data class CompletePaymentViewInfo(
        override val purchaseType: PurchaseType,
        override val amount: String,
        override val installment: String,
        override val trackId: String,
        override val authDate: String,
        override val authCode: String,
        override val trxId: String,
        override val cardNumber: String
    ): ApprovedPaymentType(), Serializable {
        fun toCancelPaymentInfo() = OfflinePaymentVM.OfflinePaymentInfo.Cancel(
            amount = amount,
            installment = installment,
            trackId = null,
            rootTrxId = trxId,
            authCode = authCode,
            authDate = authDate.substring(2, 8)
        )
    }

    data class PaymentHistoryViewInfo(
        override val purchaseType: PurchaseType,
        override val amount: String,
        override val installment: String,
        override val trackId: String,
        override val authDate: String,
        override val authCode: String,
        override val trxId: String,
        override val cardNumber: String,
        val issuerName: String,
        val rootRegDate: String? = null
    ): ApprovedPaymentType(), Serializable {
        fun toCancelPaymentInfo() = OfflinePaymentVM.OfflinePaymentInfo.Cancel(
            amount = amount,
            installment = installment,
            trackId = null,
            rootTrxId = trxId,
            authCode = authCode,
            authDate = authDate.substring(2, 8)
        )
    }
}