package com.kwonps.domain.repository

import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.paymentHistory.DailyAndMonthlyPaymentStatisticData
import com.kwonps.domain.model.paymentHistory.PaymentHistoryData
import com.kwonps.domain.model.paymentHistory.PaymentStatisticData
import com.kwonps.domain.model.paymentHistory.PeriodData
import kotlinx.coroutines.flow.Flow

interface PaymentHistoryRepository {
    suspend fun searchPaymentList(
        periodInfo: PeriodData
    ): Flow<ApiResult<List<PaymentHistoryData>>>

    suspend fun searchPaymentStatistic(
        periodInfo: PeriodData
    ): Flow<ApiResult<PaymentStatisticData>>

    suspend fun searchPaymentStatistic(): Flow<ApiResult<DailyAndMonthlyPaymentStatisticData>>

    suspend fun checkDirectPayment(trxId: String): Flow<ApiResult<Boolean>>
}