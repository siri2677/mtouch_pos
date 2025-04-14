package com.kwonps.data.remote.dto.response

sealed interface ResponsePaymentHistory {

    data class GetPaymentList(
        val result: String,
        val list: List<PaymentContents>
    ) : ResponsePaymentHistory

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
    ) : ResponsePaymentHistory

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
    ): ResponsePaymentHistory

    data class DirectPaymentCheck(
        val trackId: String,
        val trxType: String,
        val trxId: String
    ): ResponsePaymentHistory
}