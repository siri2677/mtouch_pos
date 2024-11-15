package com.example.domain.usecase.device

import com.example.domain.usecase.device.manager.CommunicateDeviceManager
import com.example.domain.usecase.device.manager.ConnectDeviceManager
import com.example.domain.model.device.DeviceCommunicateResponseData
import com.example.domain.model.device.KsnetCardReaderRequestBuilder
import com.example.domain.model.device.KsnetCardReaderResponseBuilder
import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.VanData
import com.example.domain.repositoryInterface.OfflinePaymentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext

class CommunicateKsnetCardReader(
    private val offlinePaymentRepository: OfflinePaymentRepository,
    private val fetchConnectedDeviceInfo: FetchConnectedDeviceInfo,
    private val deviceOperationCallback: DeviceCommunicateResponseData,
    private val deviceConnectManager: ConnectDeviceManager,
    private val deviceCommunicateManager: CommunicateDeviceManager,
    private val deviceConnect: ConnectCardReader,
    private val ksnetCardReaderResponseBuilder: KsnetCardReaderResponseBuilder,
    private val ksnetCardReaderRequestBuilder: KsnetCardReaderRequestBuilder
) {
    private lateinit var job: Job

    operator fun invoke(
        offlinePaymentData: OfflinePaymentData,
        paymentVanInfo: VanData
    ): Flow<ApiResult<PaymentProcessStatus>> = channelFlow {
        if (::job.isInitialized && job.isActive) { job.cancel() }
        job = launch(coroutineContext) {
            deviceOperationCallback.deviceSerialCommunicate.flatMapConcat { deviceSerial ->
                when (val receiveData = ksnetCardReaderResponseBuilder.receiveData(deviceSerial)) {
                    is PaymentProcessStatus.CompleteDeviceCommunication -> {
                        handleRequestSocketCommunication(
                            paymentInfo = offlinePaymentData,
                            paymentVanInfo = paymentVanInfo,
                            resultCommunicateData = receiveData
                        )
                    }
                    is PaymentProcessStatus.DeviceCommunication.InsertIC ->
                        handleIcCardInsertRequest(offlinePaymentData.amountData.totalAmount, receiveData)
                    is PaymentProcessStatus.DeviceCommunication.ReadingIC ->
                        handlePaymentProgressing(receiveData)
                    is PaymentProcessStatus.DeviceCommunication.FallBack ->
                        handleFallBackMessage(receiveData)
                    else -> flow {}
                }
            }.collect { send(it) }
        }

        initializeDevice()?.let { send(it) }
    }

    private suspend fun initializeDevice(): ApiResult<PaymentProcessStatus>? {
        val deviceInfo = fetchConnectedDeviceInfo()
        return if(deviceInfo != null) {
            deviceCommunicateManager.connect(
                deviceInfo.deviceInformation
            ).collect {
                delay(100)
                println("sendData")
                deviceCommunicateManager.sendData(ksnetCardReaderRequestBuilder.initDevice())
            }
            null
        } else {
            ApiResult.Error("장비 설정 후 결제 진행 해주시기 바랍니다.")
        }
    }

    private suspend fun handleRequestSocketCommunication(
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

    private suspend fun handleIcCardInsertRequest(
        amount: Int,
        deviceCommunication: PaymentProcessStatus.DeviceCommunication.InsertIC
    ): Flow<ApiResult<PaymentProcessStatus.DeviceCommunication>> = flow {
        emit(ApiResult.Success(deviceCommunication))
        deviceCommunicateManager.sendData(
            ksnetCardReaderRequestBuilder.makeCardNumSendReq(
                String.format("%09d", amount).toByteArray(),
                "99".toByteArray()
            )
        )
    }

    private suspend fun handlePaymentProgressing(
        deviceCommunication: PaymentProcessStatus.DeviceCommunication.ReadingIC
    ): Flow<ApiResult<PaymentProcessStatus.DeviceCommunication>> = flow {
        emit(ApiResult.Success(deviceCommunication))
    }

    private suspend fun handleFallBackMessage(
        deviceCommunication: PaymentProcessStatus.DeviceCommunication.FallBack
    ): Flow<ApiResult<PaymentProcessStatus.DeviceCommunication>> = flow {
        emit(ApiResult.Success(deviceCommunication))
        deviceCommunicateManager.sendData(
            ksnetCardReaderRequestBuilder.makeFallBackCardReq(deviceCommunication.code, "99")
        )
    }
}