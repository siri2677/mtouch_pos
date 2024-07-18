package com.example.domain.usecase.directPayment

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.DirectPaymentData
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.repositoryInterface.DirectPaymentRepository
import kotlinx.coroutines.flow.Flow

class DirectPayment(private val directPaymentRepository: DirectPaymentRepository) {
    suspend operator fun invoke(
        paymentInfo: OfflinePaymentData,
        directPaymentInfo: DirectPaymentData
    ): Flow<ApiResult<PaymentDetailData>> = directPaymentRepository.approve(
        directPaymentInfo = directPaymentInfo,
        paymentInfo = paymentInfo
    )
}