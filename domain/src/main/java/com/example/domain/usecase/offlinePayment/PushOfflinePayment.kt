package com.example.domain.usecase.offlinePayment

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.OfflinePaymentPushData
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.repository.OfflinePaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class PushOfflinePayment(private val offlinePaymentRepository: OfflinePaymentRepository) {
    suspend operator fun invoke(
        offlinePaymentPushData: OfflinePaymentPushData
    ): Flow<ApiResult<PaymentProcessStatus>> = flow {
        offlinePaymentRepository.push(offlinePaymentPushData).map {
            when (it) {
                is ApiResult.Error -> it
                is ApiResult.Exception -> it
                is ApiResult.Success -> ApiResult.Success(PaymentProcessStatus.CompletePayment(it.value))
            }
        }.collect { emit(it) }
    }
}