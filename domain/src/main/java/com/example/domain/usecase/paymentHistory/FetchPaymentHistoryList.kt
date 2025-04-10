package com.example.domain.usecase.paymentHistory

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.paymentHistory.PaymentHistoryData
import com.example.domain.model.paymentHistory.PeriodData
import com.example.domain.repository.PaymentHistoryRepository
import kotlinx.coroutines.flow.Flow

class FetchPaymentHistoryList(private val paymentHistoryRepository: PaymentHistoryRepository) {
    suspend operator fun invoke(
        periodInfo: PeriodData
    ): Flow<ApiResult<List<PaymentHistoryData>>> = paymentHistoryRepository.searchPaymentList(periodInfo)
}