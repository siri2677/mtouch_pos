package com.kwonps.domain.usecase.offlinePayment

import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.model.payment.OfflinePaymentData
import com.kwonps.domain.model.payment.OfflinePaymentPushData
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.model.payment.PaymentResult
import com.kwonps.domain.model.payment.VanData
import com.kwonps.domain.repository.OfflinePaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeOfflinePaymentRepository : OfflinePaymentRepository {
    var requestPaymentResponses: List<PaymentResult<VanData>> = emptyList()
    var communicateResponses: List<PaymentResult<PaymentDetailData>> = emptyList()
    var pushResponses: List<PaymentResult<PaymentDetailData>> = emptyList()

    override suspend fun requestPayment(offlinePaymentData: OfflinePaymentData): Flow<PaymentResult<VanData>> = flow {
        requestPaymentResponses.forEach { emit(it) }
    }

    override suspend fun communicateWithVan(
        resultCommunicateData: CardReaderStatus.Communication.result,
        offlinePaymentData: OfflinePaymentData,
        paymentVanInfo: VanData
    ): Flow<PaymentResult<PaymentDetailData>> = flow {
        communicateResponses.forEach { emit(it) }
    }

    override suspend fun pushReceipt(offlinePaymentPushData: OfflinePaymentPushData): Flow<PaymentResult<PaymentDetailData>> = flow {
        pushResponses.forEach { emit(it) }
    }
}
