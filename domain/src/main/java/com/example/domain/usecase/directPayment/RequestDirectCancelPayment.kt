package com.example.domain.usecase.directPayment

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.DirectPaymentData
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.repositoryInterface.DirectPaymentRepository
import kotlinx.coroutines.flow.Flow

class RequestDirectCancelPayment(private val directPaymentRepository: DirectPaymentRepository) {
    suspend operator fun invoke(
        directPaymentData: DirectPaymentData
    ): Flow<ApiResult<PaymentDetailData>> = directPaymentRepository.cancel(
        directPaymentData as DirectPaymentData.Cancel
    )
}