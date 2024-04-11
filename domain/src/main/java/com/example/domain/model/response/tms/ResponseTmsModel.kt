package com.example.domain.model.response.tms

import com.example.domain.model.request.tms.RequestTmsModel
import com.example.domain.model.response.ResponseModel

sealed interface ResponseTmsModel: ResponseModel {
    object Init: ResponseTmsModel

    data class Payment(
        val result: String,
        val van: String,
        val vanId: String,
        val trackId: String,
        val secondKey: String,
        val authCd: String?,
        val regDay: String?,
        val amount: Int?,
        val installment: String?,
        val trxId: String?
    ): ResponseTmsModel

    data class DirectPaymentCheck(
        val trxId: String
    ):ResponseTmsModel

    data class GetMerchantName(
        val idType: String,
        val name: String,
        val mchtId: String
    ):ResponseTmsModel

    data class GetPaymentList(
        val result: String,
        val list: List<PaymentContents>
    ): ResponseTmsModel

    data class PaymentContents(
        val rfdTime: String,
        val amount: String,
        val van: String,
        val vanTrxId: String,
        val authCd: String,
        val tmnId: String,
        val trackId: String,
        val bin: String,
        val cardType: String,
        val trxId: String,
        val issuer: String,
        val regDay: String,
        val resultMsg: String,
        val number: String,
        val trxResult: String,
        val regTime: String,
        val vanId: String,
        val _idx: String,
        val installment: String,
        val rfdDay: String,
        val mchtId: String,
        val brand: String,
        val rfdId: String
    )

    data class GetPaymentStatistics(
        val amount: String,
        val startDay: String,
        val cnt: String,
        val payCnt: String,
        val rfdAmt: String,
        val payAmt: String,
        val rfdCnt: String,
        val result: String,
        val _idx: String,
        val endDay: String
    ): ResponseTmsModel

    data class GetSummaryPaymentStatistics(
        val result: String,
        val _idx: String,
        val monthPayCnt: String,
        val dayRef: String,
        val monthPay: String,
        val today: String,
        val dayRefCnt: String,
        val monthRefCnt: String,
        val dayPay: String,
        val dayPayCnt: String,
        val monthRef: String
    ):ResponseTmsModel

    data class GetUserInformation(
        val tmnId: String,
        val bankName: String,
        val phoneNo: String,
        val result: String,
        val Authorization: String,
        val semiAuth: String,
        val id: String,
        val accntHolder: String,
        val appDirect: String,
        val ceoName: String,
        val addr: String,
        val key: String,
        val agencyEmail: String,
        val distEmail: String,
        val vat: String,
        val agencyTel: String,
        val agencyName: String,
        val telNo: String,
        val apiMaxInstall: String,
        val distTel: String,
        val name: String,
        val distName: String,
        val payKey: String,
        val account: String
    ): ResponseTmsModel

    data class KsnetSocketCommunicate(
        val result: String,
        val resultMsg: String?,
        val resultData: KsnetSocketCommunicateResultData?,
        val trxId: String?
    ): ResponseTmsModel

    data class KsnetSocketCommunicateResultData(
        val result: String,
        val paymentType: String,
        val telegramType: String,
        val enterpriseInfo: String,
        val serialNum: String,
        val ksnetCode: String,
        val message1: String,
        val message2: String,
        val vanTrxId: String,
        val cardType: String,
        val cardNum: String,
        val authDate: String,
        val authNum: String,
        val merchantID: String,
        val issuerCode: String,
        val issuerName: String,
        val purchaseCode: String,
        val purchaseName: String,
        val balance: String,
        val point1: String,
        val point2: String,
        val point3: String,
        val notice1: String,
        val notice2: String,
        val filler: String?,
        val installment: String,
        val trackId: String,
        val totalAmount: String,
        val serviceAmount: String,
        val taxAmount: String,
        val freeAmount: String,
        val supplyAmount: String,
        val posEntryMode: String,
        val swModelNum: String
    ): ResponseTmsModel

    data class Error(
        val message: String
    ): ResponseTmsModel

}