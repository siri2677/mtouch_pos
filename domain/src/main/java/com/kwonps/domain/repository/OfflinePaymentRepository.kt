package com.kwonps.domain.repository

import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.model.payment.OfflinePaymentData
import com.kwonps.domain.model.payment.OfflinePaymentPushData
import com.kwonps.domain.model.payment.VanData
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