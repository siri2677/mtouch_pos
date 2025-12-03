package com.kwonps.domain.usecase.offlinePayment

import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.model.payment.PaymentResult
import com.kwonps.domain.model.payment.OfflinePaymentPushData
import com.kwonps.domain.repository.OfflinePaymentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncReceipt @Inject constructor(
    private val offlinePaymentRepository: OfflinePaymentRepository
) {
    operator fun invoke(payload: OfflinePaymentPushData): Flow<PaymentResult<PaymentDetailData>> =
        offlinePaymentRepository.pushReceipt(payload)
}
