package com.example.domain.repository

import com.example.domain.model.ApiResult
import com.example.domain.model.cardreader.CardReaderStatus
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.OfflinePaymentPushData
import com.example.domain.model.payment.VanData
import kotlinx.coroutines.flow.Flow

interface OfflinePaymentRepository {
    suspend operator fun invoke(
        offlinePaymentData: OfflinePaymentData
    ): Flow<ApiResult<VanData>>

    suspend fun ksnetSocketCommunicate(
        resultCommunicateData: CardReaderStatus.Communication.result,
        offlinePaymentData: OfflinePaymentData,
        paymentVanInfo: VanData
    ): Flow<ApiResult<PaymentDetailData>>

    suspend fun push(
        offlinePaymentPushData: OfflinePaymentPushData
    ): Flow<ApiResult<PaymentDetailData>>
}