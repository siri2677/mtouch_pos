package com.kwonps.domain.usecase.directPayment

import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.payment.DirectPaymentData
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.repository.DirectPaymentRepository
import kotlinx.coroutines.flow.Flow

class RequestDirectCancelPayment(private val directPaymentRepository: DirectPaymentRepository) {
    suspend operator fun invoke(
        directPaymentData: DirectPaymentData
    ): Flow<ApiResult<PaymentDetailData>> = directPaymentRepository.cancel(
        directPaymentData as DirectPaymentData.Cancel
    )
}