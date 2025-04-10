package com.example.data.repositoryImpl

import com.example.data.remote.dto.request.RequestPaymentHistory
import com.example.data.remote.DataFormat
import com.example.data.remote.apiservice.TmsAPIService
import com.example.data.remote.dto.response.ResponsePaymentHistory
import com.example.data.remote.handleApiResult
import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.paymentHistory.DailyAndMonthlyPaymentStatisticData
import com.example.domain.model.paymentHistory.PaymentHistoryData
import com.example.domain.model.paymentHistory.PaymentStatisticData
import com.example.domain.model.paymentHistory.PeriodData
import com.example.domain.repository.PaymentHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PaymentHistoryRepositoryImpl @Inject constructor(
    private val apiService: TmsAPIService,
    private val token: String
): PaymentHistoryRepository {
    override suspend fun searchPaymentList(periodInfo: PeriodData): Flow<ApiResult<List<PaymentHistoryData>>> = flow {
        val response = apiService.list(
            token = token,
            body = DataFormat(periodInfo.toRequestGetPaymentListModel())
        )
        emit(response.handleApiResult { it.data.list.toListPaymentDetailInfo() })
    }

    override suspend fun searchPaymentStatistic(periodInfo: PeriodData): Flow<ApiResult<PaymentStatisticData>> = flow {
        val response = apiService.statistics(
            token = token,
            body = DataFormat(periodInfo.toRequestGetPaymentStatisticsModel())
        )
        emit(response.handleApiResult { it.data.toPaymentStatistic() })
    }

    override suspend fun searchPaymentStatistic(): Flow<ApiResult<DailyAndMonthlyPaymentStatisticData>> = flow {
        with(apiService.summary(token)) {
            emit(handleApiResult { it.data.toPaymentStatistic() })
        }
    }

    override suspend fun checkDirectPayment(trxId: String): Flow<ApiResult<Boolean>> = flow {
        with(apiService.check(token, DataFormat(RequestPaymentHistory.DirectPaymentCheck(trxId)))) {
            emit(handleApiResult { it.data.trxType == "ONTR" })
        }
    }

    private fun PeriodData.toRequestGetPaymentListModel() = RequestPaymentHistory.GetPaymentList(
        startDay = first,
        endDay = last,
        lastRegTime = null,
        lastRegDay = null
    )

    private fun PeriodData.toRequestGetPaymentStatisticsModel() = RequestPaymentHistory.GetPaymentStatistics(
        startDay = first,
        endDay = last
    )

    private fun List<ResponsePaymentHistory.PaymentContents>.toListPaymentDetailInfo() = map {
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

    private fun ResponsePaymentHistory.GetSummaryPaymentStatistics.toPaymentStatistic() = DailyAndMonthlyPaymentStatisticData(
        today = PaymentStatisticData(
            approveAmount = dayPay,
            approveCount = dayPayCnt,
            cancelAmount = dayRef,
            cancelCount = dayRefCnt
        ),
        month = PaymentStatisticData(
            approveAmount = monthPay,
            approveCount = monthPayCnt,
            cancelAmount = monthRef,
            cancelCount = monthRefCnt
        )
    )

    private fun ResponsePaymentHistory.GetPaymentStatistics.toPaymentStatistic() = PaymentStatisticData(
        approveAmount = payAmt,
        approveCount = payCnt,
        cancelAmount = rfdAmt,
        cancelCount = rfdCnt
    )
}