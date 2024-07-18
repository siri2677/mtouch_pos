package com.example.domain.usecase.paymentHistory

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.paymentHistory.PeriodData
import com.example.domain.repositoryInterface.PaymentHistoryRepository
import kotlinx.coroutines.flow.Flow

class FetchPaymentHistoryList(private val paymentHistoryRepository: PaymentHistoryRepository) {
    suspend operator fun invoke(
        periodInfo: PeriodData
    ): Flow<ApiResult<List<PaymentDetailData>>> = paymentHistoryRepository.searchPaymentList(periodInfo)
}