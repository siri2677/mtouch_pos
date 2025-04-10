package com.example.domain.repository

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.paymentHistory.DailyAndMonthlyPaymentStatisticData
import com.example.domain.model.paymentHistory.PaymentHistoryData
import com.example.domain.model.paymentHistory.PaymentStatisticData
import com.example.domain.model.paymentHistory.PeriodData
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