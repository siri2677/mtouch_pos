package com.kwonps.domain.usecase.offlinePayment

import com.kwonps.domain.model.payment.PaymentResult
import com.kwonps.domain.model.payment.OfflinePaymentData
import com.kwonps.domain.model.payment.VanData
import com.kwonps.domain.repository.OfflinePaymentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RequestOfflinePayment @Inject constructor(
    private val offlinePaymentRepository: OfflinePaymentRepository
) {
    suspend operator fun invoke(
        offlinePaymentData: OfflinePaymentData
    ): Flow<PaymentResult<VanData>> = offlinePaymentRepository.requestPayment(offlinePaymentData)
}
