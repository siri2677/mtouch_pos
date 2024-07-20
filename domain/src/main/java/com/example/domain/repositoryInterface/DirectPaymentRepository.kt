package com.example.domain.repositoryInterface

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.CardData
import com.example.domain.model.payment.DirectPaymentData
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.RootPaymentData
import kotlinx.coroutines.flow.Flow

interface DirectPaymentRepository {
    suspend fun approve(
        paymentInfo: OfflinePaymentData,
        directPaymentInfo: DirectPaymentData
    ): Flow<ApiResult<PaymentDetailData>>

    suspend fun cancel(
        paymentInfo: OfflinePaymentData,
        rootPaymentInfo: RootPaymentData,
        cardInfo: CardData
    ): Flow<ApiResult<PaymentDetailData>>
}