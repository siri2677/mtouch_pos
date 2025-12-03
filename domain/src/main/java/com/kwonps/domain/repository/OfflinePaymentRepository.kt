package com.kwonps.domain.repository

import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.model.payment.PaymentResult
import com.kwonps.domain.model.payment.OfflinePaymentData
import com.kwonps.domain.model.payment.OfflinePaymentPushData
import com.kwonps.domain.model.payment.VanData
import kotlinx.coroutines.flow.Flow

interface OfflinePaymentRepository {
    suspend fun requestPayment(
        offlinePaymentData: OfflinePaymentData
    ): Flow<PaymentResult<VanData>>

    suspend fun communicateWithVan(
        resultCommunicateData: CardReaderStatus.Communication.result,
        offlinePaymentData: OfflinePaymentData,
        paymentVanInfo: VanData
    ): Flow<PaymentResult<PaymentDetailData>>

    suspend fun pushReceipt(
        offlinePaymentPushData: OfflinePaymentPushData
    ): Flow<PaymentResult<PaymentDetailData>>
}
