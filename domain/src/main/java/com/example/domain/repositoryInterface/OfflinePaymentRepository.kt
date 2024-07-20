package com.example.domain.repositoryInterface

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.PaymentVanData
import com.example.domain.model.payment.RootPaymentData
import kotlinx.coroutines.flow.Flow

interface OfflinePaymentRepository {
    suspend operator fun invoke(
        offlinePaymentData: OfflinePaymentData,
        rootPaymentInfo: RootPaymentData?
    ): Flow<ApiResult<PaymentVanData>>

    suspend fun ksnetSocketCommunicate(
        resultCommunicateData: PaymentProcessStatus.CompleteDeviceCommunication,
        rootPaymentInfo: RootPaymentData?,
        paymentInfo: OfflinePaymentData,
        paymentVanInfo: PaymentVanData
    ): Flow<ApiResult<PaymentDetailData>>
}