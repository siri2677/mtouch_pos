package com.kwonps.domain.usecase.paymentHistory

import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.paymentHistory.PaymentStatisticData
import com.kwonps.domain.model.paymentHistory.PeriodData
import com.kwonps.domain.repository.PaymentHistoryRepository
import kotlinx.coroutines.flow.Flow

class FetchPaymentHistoryStatistics(private val paymentHistoryRepository: PaymentHistoryRepository) {
    suspend operator fun invoke(
        periodInfo: PeriodData
    ): Flow<ApiResult<PaymentStatisticData>> = paymentHistoryRepository.searchPaymentStatistic(periodInfo)
}