package com.kwonps.data.remote.dto.response

import com.google.gson.annotations.SerializedName

sealed interface ResponseOffPayment {
    data class Payment(
        val amount: Int?,
        val authCd: String?,
        val regDay: String?,
        val result: String,
        val van: String,
        val vanId: String,
        val secondKey: String,
        val trackId: String,
    ): ResponseOffPayment

    data class Push(
        val trxId: String,
        val resultCd: String,
        val resultMsg: String,
        val result: PushResultData?
    ): ResponseOffPayment

    data class PushResultData(
        @SerializedName("totalAmount") val amount: String,
        val installment: String,
        @SerializedName("authNum") val authCode: String,
        val authDate: String,
        val trackId: String,
        @SerializedName("telegramType") val trxResult: String,
        @SerializedName("cardNum") val cardNumber: String,
        val issuerName: String
    )

    data class KsnetSocketCommunicate(
        val result: String,
        val resultMsg: String?,
        val resultData: KsnetSocketCommunicateResultData?,
        val trxId: String?
    ): ResponseOffPayment

    data class KsnetSocketCommunicateResultData(
        @SerializedName("Status") val result: String,
        @SerializedName("TelegramType") val telegramType: String,
        @SerializedName("Enterprise_Info") val enterpriseInfo: String,
        @SerializedName("ReaderModelNum") val readerModelNum: String,
        @SerializedName("VanTr") val vanTrxId: String,
        @SerializedName("point1") val point1: String,
        @SerializedName("point2") val point2: String,
        @SerializedName("point3") val point3: String,
        @SerializedName("Message1") val message1: String,
        @SerializedName("Message2") val message2: String,
        @SerializedName("notice1") val notice1: String,
        @SerializedName("notice2") val notice2: String,
        @SerializedName("KSNETCode") val ksnetCode: String,
        @SerializedName("CardType") val cardType: String,
        @SerializedName("Classification") val classification: String,
        @SerializedName("Pos_Entry_Mode") val posEntryMode: String,
        @SerializedName("Remain") val remain: String,
        @SerializedName("SWModelNum") val swModelNum: String,
        @SerializedName("Full_Text_Num") val fullTextNum: String,
        @SerializedName("CardNo") val cardNum: String,
        @SerializedName("FranchiseID") val merchantID: String,
        @SerializedName("Gubun") val balance: String,
        @SerializedName("AuthNum") val authNum: String,
        @SerializedName("Authdate") val authDate: String,
        @SerializedName("CardName") val issuerName: String,
        @SerializedName("IssueCode") val issuerCode: String,
        @SerializedName("PurchaseName") val purchaseName: String,
        @SerializedName("PurchaseCode") val purchaseCode: String,
        @SerializedName("TotalAmount") val totalAmount: String,
        @SerializedName("Tax") val taxAmount: String,
        @SerializedName("Free") val freeAmount: String,
        @SerializedName("Amount") val supplyAmount: String,
        @SerializedName("Service_Money") val serviceAmount: String,
    )
}