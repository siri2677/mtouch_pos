package com.example.domain.model.request.tms

import com.example.domain.model.request.RequestModel
import com.google.gson.annotations.SerializedName

sealed interface RequestTmsModel: RequestModel {

    data class CancelPayment(
        val amount: String,
        val installment: String,
        val trxId: String
    ): RequestTmsModel

    data class DirectPaymentCheck(
        val trxId: String
    ): RequestTmsModel

    data class GetMerchantName(
        val mchtId: String
    ): RequestTmsModel

    class GetSummaryPaymentStatistics(): RequestTmsModel

    data class GetPaymentList(
        val startDay: String,
        val endDay: String,
        val lastRegTime: String?,
        val lastRegDay: String?
    ): RequestTmsModel

    data class GetPaymentStatistics(
        val startDay: String,
        val endDay: String
    ): RequestTmsModel

    data class GetUserInformation(
        val tmnId: String,
        val serial: String,
        val appId: String?,
        val mchtId: String,
        val version: String?,
        val telNo: String?
    ): RequestTmsModel

    data class KsnetSocketCommunicate(
        @SerializedName("tms")
        val requestKsnetSocketCommunicateTms: KsnetSocketCommunicateTms
    ): RequestTmsModel

    data class KsnetSocketCommunicateTms(
        @SerializedName("data")
        val requestKsnetSocketCommunicateData: KsnetSocketCommunicateData,
        @SerializedName("socket")
        val requestKsnetSocketCommunicateSocket: KsnetSocketCommunicateSocket
    )

    data class KsnetSocketCommunicateData(
        val van: String,
        val vanId: String,
        val trackId: String?,
        val trxId: String?,
        val walletSettle: String,
        @SerializedName("isVanPayment")
        val vanPayment: String,
        @SerializedName("number")
        val cardNumber: String,
    )

    data class KsnetSocketCommunicateSocket(
        val transType: ByteArray,
        val swModelNumber: ByteArray,
        val receiptNo: ByteArray,
        val workType: ByteArray,
        val posEntry: ByteArray,
        val filler: ByteArray,
        val signData: ByteArray,
        val telegramType: ByteArray,
        val dptId: ByteArray,
        val payType: ByteArray,
        val totalAmount: ByteArray,
        val amount: ByteArray,
        @SerializedName("servicAmount")
        val serviceAmount: ByteArray,
        val taxAmount: ByteArray,
        val freeAmount: ByteArray,
        val signTran : ByteArray,
        val readerModelNum: ByteArray,
        val encryptInfo: ByteArray,
        val reqEMVData: ByteArray,
        val trackII: ByteArray,
        val rootAuthCode: ByteArray?,
        val rootRegDate: ByteArray?
    )

    data class Payment(
        val amount: String,
        val installment: String
    ):RequestTmsModel
}