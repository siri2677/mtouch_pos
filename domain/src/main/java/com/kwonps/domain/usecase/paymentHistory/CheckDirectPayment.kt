package com.kwonps.domain.usecase.paymentHistory

import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.repository.PaymentHistoryRepository
import kotlinx.coroutines.flow.Flow

class CheckDirectPayment(private val paymentHistoryRepository: PaymentHistoryRepository) {
    suspend operator fun invoke(
        trxId: String
    ): Flow<ApiResult<Boolean>> = paymentHistoryRepository.checkDirectPayment(trxId)
}
