package com.kwonps.domain.usecase.directPayment

import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.payment.DirectPaymentData
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.repository.DirectPaymentRepository
import kotlinx.coroutines.flow.Flow

class RequestDirectPayment(private val directPaymentRepository: DirectPaymentRepository) {
    suspend operator fun invoke(
        directPaymentInfo: DirectPaymentData
    ): Flow<ApiResult<PaymentDetailData>> = directPaymentRepository.approve(
        directPaymentInfo = directPaymentInfo as DirectPaymentData.Approve
    )
}