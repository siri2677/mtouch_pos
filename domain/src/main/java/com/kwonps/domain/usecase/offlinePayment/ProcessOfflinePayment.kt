package com.kwonps.domain.usecase.offlinePayment

import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.model.payment.PaymentResult
import com.kwonps.domain.model.payment.OfflinePaymentData
import com.kwonps.domain.model.payment.VanData
import com.kwonps.domain.repository.OfflinePaymentRepository
import com.kwonps.domain.service.payment.PaymentValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class ProcessOfflinePayment @Inject constructor(
    private val offlinePaymentRepository: OfflinePaymentRepository
) {
    data class Input(
        val payment: OfflinePaymentData,
        val communicationResult: CardReaderStatus.Communication.result,
        val presetVanData: VanData? = null
    )

    data class Output(
        val paymentDetail: PaymentDetailData,
        val vanData: VanData
    )

    operator fun invoke(input: Input): Flow<PaymentResult<Output>> = flow {
        PaymentValidator.validateAmount(input.payment.amountData)?.let {
            emit(PaymentResult.Failure(it))
            return@flow
        }

        val vanFlow = input.presetVanData?.let { flowOf(PaymentResult.Success(it)) }
            ?: offlinePaymentRepository.requestPayment(input.payment)

        vanFlow.collect { vanResult ->
            when (vanResult) {
                is PaymentResult.Failure -> emit(vanResult)
                is PaymentResult.Success ->
                    offlinePaymentRepository.communicateWithVan(
                        offlinePaymentData = input.payment,
                        paymentVanInfo = vanResult.value,
                        resultCommunicateData = input.communicationResult
                    ).collect { detailResult ->
                        when (detailResult) {
                            is PaymentResult.Success -> emit(
                                PaymentResult.Success(
                                    Output(
                                        paymentDetail = detailResult.value,
                                        vanData = vanResult.value
                                    )
                                )
                            )

                            is PaymentResult.Failure -> emit(detailResult)
                        }
                    }
            }
        }
    }
}
