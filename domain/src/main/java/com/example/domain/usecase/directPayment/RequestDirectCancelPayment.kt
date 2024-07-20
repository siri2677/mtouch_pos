package com.example.domain.usecase.directPayment

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.CardData
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.RootPaymentData
import com.example.domain.repositoryInterface.DirectPaymentRepository
import kotlinx.coroutines.flow.Flow

class RequestDirectCancelPayment(private val directPaymentRepository: DirectPaymentRepository) {
    suspend operator fun invoke(
        paymentInfo: OfflinePaymentData,
        rootPaymentInfo: RootPaymentData,
        cardInfo: CardData
    ): Flow<ApiResult<PaymentDetailData>> = directPaymentRepository.cancel(
        paymentInfo = paymentInfo,
        rootPaymentInfo = rootPaymentInfo,
        cardInfo = cardInfo
    )
}