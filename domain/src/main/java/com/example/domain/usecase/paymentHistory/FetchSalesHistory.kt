package com.example.domain.usecase.paymentHistory

import com.example.domain.model.ApiResult
import com.example.domain.model.paymentHistory.DailyAndMonthlyPaymentStatisticData
import com.example.domain.repository.PaymentHistoryRepository
import kotlinx.coroutines.flow.Flow

class FetchSalesHistory(private val paymentHistoryRepository: PaymentHistoryRepository) {
    suspend operator fun invoke(): Flow<ApiResult<DailyAndMonthlyPaymentStatisticData>> = paymentHistoryRepository.searchPaymentStatistic()
}