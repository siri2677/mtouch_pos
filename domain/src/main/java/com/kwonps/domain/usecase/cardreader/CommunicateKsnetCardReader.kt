package com.kwonps.domain.usecase.cardreader

import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.model.cardreader.KsnetCardReaderRequestBuilder
import com.kwonps.domain.model.cardreader.KsnetCardReaderResponseBuilder
import com.kwonps.domain.model.payment.OfflinePaymentData
import com.kwonps.domain.repository.CardReaderCommunicateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class CommunicateKsnetCardReader(
    private val cardReaderCommunicateRepository: CardReaderCommunicateRepository,
    private val ksnetCardReaderResponseBuilder: KsnetCardReaderResponseBuilder,
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

    @OptIn(FlowPreview::class)
    operator fun invoke(
        offlinePaymentData: OfflinePaymentData,
        deviceInfo: String
    ): Flow<CardReaderStatus> = channelFlow {
        launchJob("deviceConnectStatus") {
            cardReaderCommunicateRepository.connectionStatus.collect {
                when (it) {
                    CardReaderStatus.Connection.Active ->
                        cardReaderCommunicateRepository.sendData(ksnetCardReaderRequestBuilder.initDevice())
                    is CardReaderStatus.Connection.Establishing ->
                        send(it)
                    is CardReaderStatus.Connection.Failure ->
                        send(it)
                    else -> {}
                }
            }
        }

        launchJob("deviceSerialCommunicate") {
            cardReaderCommunicateRepository.dataStream.flatMapConcat { deviceSerial ->
                when (val receiveData = ksnetCardReaderResponseBuilder.receiveData(deviceSerial)) {
                    is CardReaderStatus.Communication.result ->
                        flow { emit(receiveData) }
                    is CardReaderStatus.Communication.InsertIC ->
                        handleIcCardInsertRequest(offlinePaymentData.amountData.totalAmount, receiveData)
                    is CardReaderStatus.Communication.ReadingIC ->
                        flow { emit(receiveData) }
                    is CardReaderStatus.Communication.FallBack ->
                        handleFallBackMessage(receiveData)
                    CardReaderStatus.Communication.Init -> flow {}
                }
            }.collect { send(it) }
        }

        delay(1)
        cardReaderCommunicateRepository.connect(deviceInfo)
    }

    private fun handleIcCardInsertRequest(
        amount: Int,
        deviceCommunication: CardReaderStatus.Communication.InsertIC
    ): Flow<CardReaderStatus.Communication> = flow {
        System.out.println("handleIcCardInsertRequest")
        emit(deviceCommunication)
        cardReaderCommunicateRepository.sendData(
            ksnetCardReaderRequestBuilder.makeCardNumSendReq(
                String.format("%09d", amount).toByteArray(), "99".toByteArray()
            )
        )
    }


    private fun handleFallBackMessage(
        deviceCommunication: CardReaderStatus.Communication.FallBack
    ): Flow<CardReaderStatus.Communication> = flow {
        emit(deviceCommunication)
        cardReaderCommunicateRepository.sendData(
            ksnetCardReaderRequestBuilder.makeFallBackCardReq(deviceCommunication.code, "99")
        )
    }

    fun init() {
        cancelJob("deviceSerialCommunicate")
        cardReaderCommunicateRepository.sendData(ksnetCardReaderRequestBuilder.initDevice())
        cardReaderCommunicateRepository.stopRetry()
    }
}