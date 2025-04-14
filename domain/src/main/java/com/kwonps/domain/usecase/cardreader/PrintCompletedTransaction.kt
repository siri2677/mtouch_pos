package com.kwonps.domain.usecase.cardreader

import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.model.cardreader.KsnetCardReaderRequestBuilder
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.model.user.UserDetailData
import com.kwonps.domain.repository.CardReaderCommunicateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlin.collections.set

class PrintCompletedTransaction(
    private val cardReaderCommunicateRepository: CardReaderCommunicateRepository,
    private val ksnetCardReaderRequestBuilder: KsnetCardReaderRequestBuilder
) {
    private val jobs = mutableMapOf<String, Job>()

    fun CoroutineScope.launchJob(key: String, block: suspend CoroutineScope.() -> Unit) {
        cancelJob(key)
        jobs[key] = launch { block() }
    }

    fun cancelJob(key: String) {
        jobs[key]?.takeIf { it.isActive }?.cancel()
    }

    operator fun invoke(
        deviceInformation: String,
        userDetailData: UserDetailData,
        paymentDetailData: PaymentDetailData
    ): Flow<CardReaderStatus> = channelFlow {
        when {
            userDetailData.van.contains("KSPAY") -> {
                launchJob("deviceConnectStatus") {
                    cardReaderCommunicateRepository.connectionStatus.collect {
                        when (it) {
                            CardReaderStatus.Connection.Active -> {
                                cardReaderCommunicateRepository.sendData(ksnetCardReaderRequestBuilder.printMode(), true)
                                cardReaderCommunicateRepository.sendData(ksnetCardReaderRequestBuilder.printReceipt(userDetailData, paymentDetailData), true)
                                cardReaderCommunicateRepository.sendData("\n".toByteArray(), true)
                                cardReaderCommunicateRepository.sendData("\n\n\n\n".toByteArray(), true)
                                cardReaderCommunicateRepository.sendData(ksnetCardReaderRequestBuilder.resetPrint(), true)
                            }
                            else -> {}
                        }
                    }
                }

                delay(1)
                cardReaderCommunicateRepository.connect(deviceInformation)
            }
        }
    }
}