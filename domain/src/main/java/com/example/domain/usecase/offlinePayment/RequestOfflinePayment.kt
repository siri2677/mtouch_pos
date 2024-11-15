package com.example.domain.usecase.offlinePayment

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.VanData
import com.example.domain.repositoryInterface.OfflinePaymentRepository
import com.example.domain.usecase.device.CommunicateKsnetCardReader
import com.example.domain.usecase.device.manager.CommunicateCardTerminalManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class RequestOfflinePayment @Inject constructor(
    private val offlinePaymentRepository: OfflinePaymentRepository,
    private val communicateKsnetCardReader: CommunicateKsnetCardReader
) {
    suspend operator fun invoke(
        offlinePaymentData: OfflinePaymentData,
        communicateCardTerminal: CommunicateCardTerminalManager?
    ): Flow<ApiResult<PaymentProcessStatus>> = flow {
        offlinePaymentRepository(offlinePaymentData).flatMapMerge { apiResult ->
            when (apiResult) {
                is ApiResult.Error -> flowOf(ApiResult.Error(apiResult.message))
                is ApiResult.Exception -> flowOf(ApiResult.Exception(apiResult.exception))
                is ApiResult.Success -> handleSuccess(
                    offlinePaymentData = offlinePaymentData,
                    communicateCardTerminal = communicateCardTerminal,
                    apiResult = apiResult.value
                )
            }
        }.collect { result -> emit(result) }
    }

    private suspend fun handleSuccess(
        offlinePaymentData: OfflinePaymentData,
        communicateCardTerminal: CommunicateCardTerminalManager?,
        apiResult: VanData
    ): Flow<ApiResult<PaymentProcessStatus>> = flow {
        communicateCardTerminal?.let { cardTerminal ->
            cardTerminal(
                paymentInfo = offlinePaymentData,
                paymentVanInfo = apiResult
            )?.let { error ->
                emit(ApiResult.Error(error))
            } ?: emit(ApiResult.Success(PaymentProcessStatus.ApprovePayment(apiResult.vanTrackId)))
        } ?: emitAll(
            communicateKsnetCardReader(
                offlinePaymentData = offlinePaymentData,
                paymentVanInfo = apiResult
            )
        )
    }
}