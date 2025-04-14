package com.kwonps.domain.usecase.offlinePayment

import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.model.payment.OfflinePaymentData
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.model.payment.VanData
import com.kwonps.domain.repository.OfflinePaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class KsnetSocketCommunicate(private val offlinePaymentRepository: OfflinePaymentRepository) {
    operator fun invoke(
        offlinePaymentData: OfflinePaymentData,
        paymentVanInfo: VanData,
        resultCommunicateData: CardReaderStatus.Communication.result
    ): Flow<ApiResult<PaymentDetailData>> = flow {
        offlinePaymentRepository.ksnetSocketCommunicate(
            resultCommunicateData = resultCommunicateData,
            offlinePaymentData = offlinePaymentData,
            paymentVanInfo = paymentVanInfo
        ).collect { emit(it) }
    }
}