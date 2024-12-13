package com.example.domain.usecase.paymentHistory

import com.example.domain.model.ApiResult
import com.example.domain.repository.PaymentHistoryRepository
import kotlinx.coroutines.flow.Flow

class CheckDirectPayment(private val paymentHistoryRepository: PaymentHistoryRepository) {
    suspend operator fun invoke(
        trxId: String
    ): Flow<ApiResult<Boolean>> = paymentHistoryRepository.checkDirectPayment(trxId)
}
