package com.kwonps.domain.usecase.offlinePayment

import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.payment.OfflinePaymentPushData
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.repository.OfflinePaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PushOfflinePayment(private val offlinePaymentRepository: OfflinePaymentRepository) {
    operator fun invoke(
        offlinePaymentPushData: OfflinePaymentPushData
    ): Flow<ApiResult<PaymentDetailData>> = flow {
        offlinePaymentRepository.push(offlinePaymentPushData).collect { emit(it) }
    }
}