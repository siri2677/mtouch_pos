package com.example.domain.usecase.device

import com.example.domain.usecase.device.manager.CommunicateDeviceManager
import com.example.domain.usecase.device.manager.ConnectDeviceManager
import com.example.domain.model.device.DeviceCommunicateResponseData
import com.example.domain.model.device.KSNetCardReaderRequestBuilder
import com.example.domain.model.device.KSNetCardReaderResponseBuilder
import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.PaymentVanData
import com.example.domain.model.payment.RootPaymentData
import com.example.domain.repositoryInterface.OfflinePaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CommunicateKsnetCardReader(
    private val offlinePaymentRepository: OfflinePaymentRepository,
    private val fetchConnectedDeviceInfo: FetchConnectedDeviceInfo,
    private val deviceOperationCallback: DeviceCommunicateResponseData,
    private val deviceConnectManager: ConnectDeviceManager,
    private val deviceCommunicateManager: CommunicateDeviceManager,
    private val deviceConnect: ConnectCardReader,
    private val ksnetCardReaderResponseBuilder: KSNetCardReaderResponseBuilder,
    private val ksnetCardReaderRequestBuilder: KSNetCardReaderRequestBuilder
) {
    private val mutex = Mutex()

    suspend operator fun invoke(
        paymentInfo: OfflinePaymentData,
        paymentVanInfo: PaymentVanData,
        rootPaymentInfo: RootPaymentData?,
    ): Flow<ApiResult<PaymentProcessStatus>> = flow {
        initDevice()

        mutex.withLock {
            deviceOperationCallback.deviceSerialCommunicate.flatMapConcat { deviceSerial ->
                when (val receiveData = ksnetCardReaderResponseBuilder.receiveData(deviceSerial)) {
                    is PaymentProcessStatus.CompleteDeviceCommunication ->
                        handleRequestSocketCommunication(
                            paymentInfo = paymentInfo,
                            rootPaymentInfo = rootPaymentInfo,
                            paymentVanInfo = paymentVanInfo,
                            resultCommunicateData = receiveData
                        )

                    is PaymentProcessStatus.DeviceCommunication.InsertIC ->
                        handleIcCardInsertRequest(paymentInfo.totalAmount, receiveData)

                    is PaymentProcessStatus.DeviceCommunication.ReadingIC ->
                        handlePaymentProgressing(receiveData)

                    is PaymentProcessStatus.DeviceCommunication.FallBack ->
                        handleFallBackMessage(receiveData)

                    else -> flow {}
                }
            }.collect { emit(it) }
        }
    }

    private suspend fun FlowCollector<ApiResult<PaymentProcessStatus>>.initDevice() {
        val deviceInfo = fetchConnectedDeviceInfo()
        if (deviceInfo == null) {
            emit(ApiResult.Error("장비 설정 후 결제 진행 해주시기 바랍니다."))
            return
        }

        val requestInit = { deviceCommunicateManager.sendData(ksnetCardReaderRequestBuilder.initDevice()) }
        if (!deviceCommunicateManager.isDeviceServiceInitialized()) {
            deviceConnect(
                deviceInfo = deviceInfo,
                deviceConnectManager = deviceConnectManager
            ).collect { requestInit() }
        } else {
            requestInit()
        }
    }

    private suspend fun handleRequestSocketCommunication(
        paymentInfo: OfflinePaymentData,
        rootPaymentInfo: RootPaymentData?,
        paymentVanInfo: PaymentVanData,
        resultCommunicateData: PaymentProcessStatus.CompleteDeviceCommunication
    ): Flow<ApiResult<PaymentProcessStatus.CompletePayment>> = flow {
        offlinePaymentRepository.ksnetSocketCommunicate(
            resultCommunicateData = resultCommunicateData,
            rootPaymentInfo = rootPaymentInfo,
            paymentInfo = paymentInfo,
            paymentVanInfo = paymentVanInfo,
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