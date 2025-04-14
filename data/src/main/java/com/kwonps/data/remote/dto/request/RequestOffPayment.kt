package com.kwonps.data.remote.dto.request

import com.google.gson.annotations.SerializedName

sealed interface RequestOffPayment {
    data class Payment(
        val amount: String,
        val installment: String,
        val trackId: String?,
    ): RequestOffPayment

    data class CancelPayment(
        val amount: String,
        val installment: String,
        val trxId: String,
        val trackId: String?
    ): RequestOffPayment

    data class Push(
        val amount: String,
        val installment: String,
        val trackId: String?,
        val rootTrxId: String?,
        val vanTrxId: String,
        val authCd: String,
        val regDate: String,
        val number: String
    )

    data class KsnetSocketCommunicate(
        @SerializedName("tms")
        val requestKsnetSocketCommunicateTms: KsnetSocketCommunicateTms
    ): RequestOffPayment

    data class KsnetSocketCommunicateTms(
        @SerializedName("data")
        val requestKsnetSocketCommunicateData: KsnetSocketCommunicateData,
        @SerializedName("socket")
        val requestKsnetSocketCommunicateSocket: KsnetSocketCommunicateSocket
    )

    data class KsnetSocketCommunicateData(
        @SerializedName("isVanPayment")
        val vanPayment: String,
        @SerializedName("number")
        val cardNumber: String,
        val walletSettle: String,
        val van: String?,
        val vanId: String?,
        val trackId: String?,
        val trxId: String?
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
}