package com.example.domain.usecase.offlinePayment

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.PaymentVanData
import com.example.domain.repositoryInterface.OfflinePaymentRepository
import com.example.domain.usecase.device.CommunicateKsnetCardReader
import com.example.domain.usecase.device.manager.CommunicateCardTerminalManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Provider

class OfflinePayment @Inject constructor(
    private val offlinePaymentRepository: OfflinePaymentRepository,
    private val communicateKsnetCardReader: CommunicateKsnetCardReader
) {
    suspend operator fun invoke(
        offlinePaymentData: OfflinePaymentData,
        communicateCardTerminal: CommunicateCardTerminalManager?
    ): Flow<ApiResult<PaymentProcessStatus>> = flow {
        offlinePaymentRepository(
            offlinePaymentData = offlinePaymentData,
            rootPaymentInfo = null
        ).flatMapMerge { apiResult ->
            when (apiResult) {
                is ApiResult.Error -> flowOf(ApiResult.Error(apiResult.message))
                is ApiResult.Exception -> flowOf(ApiResult.Exception(apiResult.exception))
                is ApiResult.Success -> handleSuccess(apiResult, communicateCardTerminal, offlinePaymentData)
            }
        }.collect { result ->
            emit(result)
        }
    }

    private fun handleSuccess(
        apiResult: ApiResult.Success<PaymentVanData>,
        communicateCardTerminal: CommunicateCardTerminalManager?,
        paymentInfo: OfflinePaymentData
    ): Flow<ApiResult<PaymentProcessStatus>> = flow {
        if (communicateCardTerminal != null) {
            val deviceInfo = communicateCardTerminal(
                paymentInfo = paymentInfo,
                paymentVanInfo = apiResult.value,
                rootPaymentInfo = null
            )
            if (deviceInfo != null) emit(ApiResult.Error(deviceInfo))
        } else {
            emitAll(
                communicateKsnetCardReader(
                    paymentInfo = paymentInfo,
                    paymentVanInfo = apiResult.value,
                    rootPaymentInfo = null
                )
            )
        }
    }
}