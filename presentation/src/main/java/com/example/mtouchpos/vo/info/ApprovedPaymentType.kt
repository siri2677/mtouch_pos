package com.example.mtouchpos.vo.info

import com.example.mtouchpos.viewmodel.OfflinePaymentVM
import com.example.mtouchpos.vo.type.PurchaseType
import com.google.gson.annotations.Expose
import java.io.Serializable

sealed class ApprovedPaymentType {
    data class CompletePaymentViewInfo(
        val purchaseType: PurchaseType,
        val totalAmount: String,
        val freeAmount: String,
        val serviceAmount: String,
        val installment: String,
        val trackId: String?,
        val authDate: String,
        val authCode: String,
        val trxId: String?,
        val issuer: String,
        val acquirer: String,
        val cardNumber: String
    ): ApprovedPaymentType(), Serializable {
        fun toCancelPaymentInfo() = OfflinePaymentVM.OfflinePaymentInfo.Cancel(
            totalAmount = totalAmount,
            installment = installment,
            trackId = null,
            rootTrxId = trxId,
            authCode = authCode,
            authDate = authDate.substring(0, 6)
        )

        fun getSupplyAmount(): String = (totalAmount.toInt() - (totalAmount.toInt() - freeAmount.toInt() / 11)).toString()

        fun getTaxAmount(): String = (totalAmount.toInt() - freeAmount.toInt() / 11).toString()
    }

    data class PaymentHistoryViewInfo(
        @Expose val resultMsg: String,
        val purchaseType: PurchaseType,
        @Expose val amount: String,
        @Expose val taxAmount: String,
        @Expose val freeAmount: String,
        @Expose val installment: String,
        @Expose val van: String,
        @Expose val vanId: String,
        @Expose val vanTrxId: String,
        @Expose val authCd: String,
        @Expose val tmnId: String,
        @Expose val mchtId: String,
        @Expose val trxId: String,
        @Expose val trackId: String,
        @Expose val bin: String,
        @Expose val cardType: String,
        @Expose val issuer: String,
        @Expose val number: String,
        @Expose val regDay: String,
        @Expose val regTime: String,
        @Expose val brand: String,
        @Expose val rfdId: String,
        @Expose val rfdDay: String,
        @Expose val rfdTime: String
    ): ApprovedPaymentType(), Serializable {
        fun toCancelPaymentInfo() = OfflinePaymentVM.OfflinePaymentInfo.Cancel(
            totalAmount = amount,
            installment = installment,
            trackId = null,
            rootTrxId = trxId,
            authCode = authCd,
            authDate = regDay.substring(2, 8)
        )
    }
}