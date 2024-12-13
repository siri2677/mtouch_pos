package com.example.domain.usecase.directPayment

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.DirectPaymentData
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.repository.DirectPaymentRepository
import kotlinx.coroutines.flow.Flow

class RequestDirectPayment(private val directPaymentRepository: DirectPaymentRepository) {
    suspend operator fun invoke(
        directPaymentInfo: DirectPaymentData
    ): Flow<ApiResult<PaymentDetailData>> = directPaymentRepository.approve(
        directPaymentInfo = directPaymentInfo as DirectPaymentData.Approve
    )
}