package com.example.domain.usecase.offlinePayment

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.OfflinePaymentPushData
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.repository.OfflinePaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class PushOfflinePayment(private val offlinePaymentRepository: OfflinePaymentRepository) {
    operator fun invoke(
        offlinePaymentPushData: OfflinePaymentPushData
    ): Flow<ApiResult<PaymentDetailData>> = flow {
        offlinePaymentRepository.push(offlinePaymentPushData).collect { emit(it) }
    }
}