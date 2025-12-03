package com.kwonps.data.mapper

import com.kwonps.data.remote.dto.request.RequestPaymentHistory
import com.kwonps.data.remote.dto.response.ResponsePaymentHistory
import com.kwonps.domain.model.paymentHistory.DailyAndMonthlyPaymentStatisticData
import com.kwonps.domain.model.paymentHistory.PaymentHistoryData
import com.kwonps.domain.model.paymentHistory.PaymentStatisticData
import com.kwonps.domain.model.paymentHistory.PeriodData
import javax.inject.Inject

class PaymentHistoryMapper @Inject constructor() {
    fun toListRequest(periodData: PeriodData): RequestPaymentHistory.GetPaymentList =
        RequestPaymentHistory.GetPaymentList(
            startDay = periodData.first,
            endDay = periodData.last,
            lastRegTime = null,
            lastRegDay = null
        )

    fun toStatisticRequest(periodData: PeriodData): RequestPaymentHistory.GetPaymentStatistics =
        RequestPaymentHistory.GetPaymentStatistics(
            startDay = periodData.first,
            endDay = periodData.last
        )

    fun toDirectPaymentCheck(trxId: String) = RequestPaymentHistory.DirectPaymentCheck(trxId)

    fun toDomainList(response: List<ResponsePaymentHistory.PaymentContents>): List<PaymentHistoryData> =
        response.map {
            PaymentHistoryData(
                rfdTime = it.rfdTime,
                amount = it.amount,
                van = it.van,
                vanTrxId = it.vanTrxId,
                authCd = it.authCd,
                tmnId = it.tmnId,
                trackId = it.trackId,
                bin = it.bin,
                cardType = it.cardType,
                trxId = it.trxId,
                issuer = it.issuer,
                regDay = it.regDay,
                resultMsg = it.resultMsg,
                number = it.number,
                trxResult = it.trxResult,
                regTime = it.regTime,
                vanId = it.vanId,
                _idx = it._idx,
                installment = it.installment,
                rfdDay = it.rfdDay,
                mchtId = it.mchtId,
                brand = it.brand,
                rfdId = it.rfdId,
            )
        }

    fun toStatistic(response: ResponsePaymentHistory.GetPaymentStatistics) = PaymentStatisticData(
        approveAmount = response.payAmt,
        approveCount = response.payCnt,
        cancelAmount = response.rfdAmt,
        cancelCount = response.rfdCnt
    )

    fun toDailyAndMonthlyStatistic(
        response: ResponsePaymentHistory.GetSummaryPaymentStatistics
    ) = DailyAndMonthlyPaymentStatisticData(
        today = PaymentStatisticData(
            approveAmount = response.dayPay,
            approveCount = response.dayPayCnt,
            cancelAmount = response.dayRef,
            cancelCount = response.dayRefCnt
        ),
        month = PaymentStatisticData(
            approveAmount = response.monthPay,
            approveCount = response.monthPayCnt,
            cancelAmount = response.monthRef,
            cancelCount = response.monthRefCnt
        )
    )
}
