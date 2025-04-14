package com.kwonps.domain.usecase.paymentHistory

import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.paymentHistory.DailyAndMonthlyPaymentStatisticData
import com.kwonps.domain.repository.PaymentHistoryRepository
import kotlinx.coroutines.flow.Flow

class FetchSalesHistory(private val paymentHistoryRepository: PaymentHistoryRepository) {
    suspend operator fun invoke(): Flow<ApiResult<DailyAndMonthlyPaymentStatisticData>> = paymentHistoryRepository.searchPaymentStatistic()
}