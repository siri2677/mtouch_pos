package com.example.domain.usecase.cardreader

import com.example.domain.repository.CardReaderCommunicateRepository
import com.example.domain.model.cardreader.CardReaderStatus
import com.example.domain.model.cardreader.KsnetCardReaderRequestBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlin.collections.set

class ConnectCardReader(
    private val cardReaderCommunicateRepository: CardReaderCommunicateRepository,
    private val ksnetCardReaderRequestBuilder: KsnetCardReaderRequestBuilder
) {
    private val jobs = mutableMapOf<String, Job>()

    private fun CoroutineScope.launchJob(key: String, block: suspend CoroutineScope.() -> Unit) {
        cancelJob(key)
        jobs[key] = launch { block() }
    }

    private fun cancelJob(key: String) {
        jobs[key]?.takeIf { it.isActive }?.cancel()
    }

    operator fun invoke(
        deviceInformation: String,
    ): Flow<CardReaderStatus.Connection> = channelFlow {
        launchJob("deviceConnectStatus") {
            cardReaderCommunicateRepository.connectionStatus.collect {
                when(it) {
                    CardReaderStatus.Connection.Active -> {
                        cardReaderCommunicateRepository.disConnect()
                    }
                    CardReaderStatus.Connection.Inactive ->
                        send(it)
                    is CardReaderStatus.Connection.Establishing ->
                        send(it)
                    else -> {}
                }
            }
        }

        delay(1)
        cardReaderCommunicateRepository.connect(deviceInformation)
    }

    fun init() {
        cardReaderCommunicateRepository.sendData(ksnetCardReaderRequestBuilder.initDevice())
        cardReaderCommunicateRepository.stopRetry()
    }
}