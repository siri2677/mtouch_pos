package com.example.domain.usecase.cardreader

import com.example.domain.manager.cardreader.CardReaderCommunicateManager
import com.example.domain.manager.cardreader.CardReaderConnectManager
import com.example.domain.manager.cardreader.CardReaderResponseManager
import com.example.domain.model.cardreader.KsnetCardReaderRequestBuilder
import com.example.domain.model.cardreader.KsnetCardReaderResponseBuilder
import com.example.domain.model.ApiResult
import com.example.domain.model.cardreader.CardReaderStatus
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.PaymentProcessStatus.ConnectReader.*
import com.example.domain.model.payment.VanData
import com.example.domain.repository.OfflinePaymentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CommunicateKsnetCardReader(
    private val offlinePaymentRepository: OfflinePaymentRepository,
    private val fetchConnectedDeviceInfo: FetchConnectedDeviceInfo,
    private val deviceOperationCallback: CardReaderResponseManager,
    private val deviceConnectManager: CardReaderConnectManager,
    private val deviceCommunicateManager: CardReaderCommunicateManager,
    private val deviceConnect: ConnectCardReader,
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
        paymentVanInfo: VanData
    ): Flow<ApiResult<PaymentProcessStatus>> = channelFlow {
        launchJob("deviceSerialCommunicate") {
            deviceOperationCallback.deviceSerialCommunicate.flatMapConcat { deviceSerial ->
                when (val receiveData = ksnetCardReaderResponseBuilder.receiveData(deviceSerial)) {
                    is PaymentProcessStatus.CompleteDeviceCommunication -> {
                        handleRequestSocketCommunication(
                            paymentInfo = offlinePaymentData,
                            paymentVanInfo = paymentVanInfo,
                            resultCommunicateData = receiveData
                        )
                    }
                    is PaymentProcessStatus.CommunicateReader.InsertIC ->
                        handleIcCardInsertRequest(offlinePaymentData.amountData.totalAmount, receiveData)
                    is PaymentProcessStatus.CommunicateReader.ReadingIC ->
                        handlePaymentProgressing(receiveData)
                    is PaymentProcessStatus.CommunicateReader.FallBack ->
                        handleFallBackMessage(receiveData)
                    else -> flow {}
                }
            }.collect { send(it) }
        }

        launchJob("deviceConnectStatus") {
            deviceOperationCallback.deviceConnectStatus.collect {
                when (it) {
                    CardReaderStatus.Connected -> {
                        delay(100)
                        deviceCommunicateManager.sendData(ksnetCardReaderRequestBuilder.initDevice())
                    }
                    is CardReaderStatus.DisConnected -> {
                        send(ApiResult.Success(Retry(it.retryCount)))
                    }
                    else -> {}
                }
            }
        }

//        if (::job.isInitialized && job.isActive) { job.cancel() }
//        job = launch(coroutineContext) {
//            deviceOperationCallback.deviceSerialCommunicate.flatMapConcat { deviceSerial ->
//                when (val receiveData = ksnetCardReaderResponseBuilder.receiveData(deviceSerial)) {
//                    is PaymentProcessStatus.CompleteDeviceCommunication -> {
//                        handleRequestSocketCommunication(
//                            paymentInfo = offlinePaymentData,
//                            paymentVanInfo = paymentVanInfo,
//                            resultCommunicateData = receiveData
//                        )
//                    }
//                    is PaymentProcessStatus.CommunicateReader.InsertIC ->
//                        handleIcCardInsertRequest(offlinePaymentData.amountData.totalAmount, receiveData)
//                    is PaymentProcessStatus.CommunicateReader.ReadingIC ->
//                        handlePaymentProgressing(receiveData)
//                    is PaymentProcessStatus.CommunicateReader.FallBack ->
//                        handleFallBackMessage(receiveData)
//                    else -> flow {}
//                }
//            }.collect { send(it) }
//        }
//
//        if (::job2.isInitialized && job2.isActive) { job2.cancel() }
//        job2 = launch(coroutineContext) {
//            deviceOperationCallback.deviceConnectStatus.collect {
//                when (it) {
//                    CardReaderStatus.Connected -> {
//                        stopRetry()
//                        delay(100)
//                        deviceCommunicateManager.sendData(ksnetCardReaderRequestBuilder.initDevice())
//                    }
//                    is CardReaderStatus.DisConnected -> {
//                        if (it.retryCount == 6) {
//                            stopRetry()
//                            send(ApiResult.Error("장치 연결에 실패하였습니다."))
//                        } else {
//                            send(ApiResult.Success(PaymentProcessStatus.ConnectReader.Retry(it.retryCount)))
//                        }
//                    }
//                }
//            }
//        }

        delay(10)
        initializeDevice()?.let { send(it) }
//        initializeDevice().collect { send(it) }
    }

    private fun initializeDevice(): ApiResult<PaymentProcessStatus>? =
        if(fetchConnectedDeviceInfo() != null) {
            deviceCommunicateManager.connect(fetchConnectedDeviceInfo()!!.deviceInformation)
            null
        } else {
            ApiResult.Error("장치 설정 후 결제 진행 해주시기 바랍니다.")
        }
//        fetchConnectedDeviceInfo()?.let {
//        deviceCommunicateManager.connect(it.deviceInformation)
//        return null
//            deviceCommunicateManager.connect(it.deviceInformation).collect {
//                when(it) {
//                    CardReaderStatus.Connected -> {
//                        stopRetry()
//                        delay(100)
//                        deviceCommunicateManager.sendData(ksnetCardReaderRequestBuilder.initDevice())
//                    }
//                    is CardReaderStatus.DisConnected -> {
//                        if(it.retryCount == 6) {
//                            stopRetry()
//                            emit(ApiResult.Error("장치 연결에 실패하였습니다."))
//                        } else {
//                            emit(ApiResult.Success(PaymentProcessStatus.ConnectReader.Retry(it.retryCount)))
//                        }
//                    }
//                }
//            }
//    } ?: return ApiResult.Error("장치 설정 후 결제 진행 해주시기 바랍니다.")

    private fun handleRequestSocketCommunication(
        paymentInfo: OfflinePaymentData,
        paymentVanInfo: VanData,
        resultCommunicateData: PaymentProcessStatus.CompleteDeviceCommunication
    ): Flow<ApiResult<PaymentProcessStatus.CompletePayment>> = flow {
        offlinePaymentRepository.ksnetSocketCommunicate(
            resultCommunicateData = resultCommunicateData,
            paymentInfo = paymentInfo,
            paymentVanInfo = paymentVanInfo
        ).map {
            when (it) {
                is ApiResult.Error -> it
                is ApiResult.Exception -> it
                is ApiResult.Success -> ApiResult.Success(PaymentProcessStatus.CompletePayment(it.value))
            }
        }.collect { emit(it) }
    }

    private fun handleIcCardInsertRequest(
        amount: Int,
        deviceCommunication: PaymentProcessStatus.CommunicateReader.InsertIC
    ): Flow<ApiResult<PaymentProcessStatus.CommunicateReader>> = flow {
        emit(ApiResult.Success(deviceCommunication))
        deviceCommunicateManager.sendData(
            ksnetCardReaderRequestBuilder.makeCardNumSendReq(
                String.format("%09d", amount).toByteArray(), "99".toByteArray()
            )
        )
    }

    private fun handlePaymentProgressing(
        deviceCommunication: PaymentProcessStatus.CommunicateReader.ReadingIC
    ): Flow<ApiResult<PaymentProcessStatus.CommunicateReader>> = flow {
        emit(ApiResult.Success(deviceCommunication))
    }

    private fun handleFallBackMessage(
        deviceCommunication: PaymentProcessStatus.CommunicateReader.FallBack
    ): Flow<ApiResult<PaymentProcessStatus.CommunicateReader>> = flow {
        emit(ApiResult.Success(deviceCommunication))
        deviceCommunicateManager.sendData(
            ksnetCardReaderRequestBuilder.makeFallBackCardReq(deviceCommunication.code, "99")
        )
    }

    fun stopRetry() {
        cancelJob("deviceSerialCommunicate")
        deviceCommunicateManager.stopRetry(ksnetCardReaderRequestBuilder.initDevice())
    }
}