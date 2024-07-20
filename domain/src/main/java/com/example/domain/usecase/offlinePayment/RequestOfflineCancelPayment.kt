package com.example.domain.usecase.offlinePayment

import com.example.domain.repositoryInterface.OfflinePaymentRepository
import com.example.domain.usecase.device.CommunicateKsnetCardReader
import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.PaymentVanData
import com.example.domain.model.payment.RootPaymentData
import com.example.domain.usecase.device.manager.CommunicateCardTerminalManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class RequestOfflineCancelPayment @Inject constructor(
    private val offlinePaymentRepository: OfflinePaymentRepository,
    private val deviceCommunicate: CommunicateKsnetCardReader
) {
    suspend operator fun invoke(
        paymentInfo: OfflinePaymentData,
        rootPaymentInfo: RootPaymentData,
        communicateCardTerminal: CommunicateCardTerminalManager?
    ): Flow<ApiResult<PaymentProcessStatus>> = flow {
        offlinePaymentRepository(
            offlinePaymentData = paymentInfo,
            rootPaymentInfo = rootPaymentInfo
        ).flatMapMerge { apiResult ->
            when (apiResult) {
                is ApiResult.Error -> flowOf(ApiResult.Error(apiResult.message))
                is ApiResult.Exception -> flowOf(ApiResult.Exception(apiResult.exception))
                is ApiResult.Success -> handleSuccess(apiResult, communicateCardTerminal, paymentInfo)
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
                deviceCommunicate(
                    paymentInfo = paymentInfo,
                    paymentVanInfo = apiResult.value,
                    rootPaymentInfo = null
                )
            )
        }
    }
}