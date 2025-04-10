package com.example.domain.usecase.offlinePayment

import com.example.domain.model.ApiResult
import com.example.domain.model.cardreader.CardReaderStatus
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.payment.VanData
import com.example.domain.repository.OfflinePaymentRepository
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