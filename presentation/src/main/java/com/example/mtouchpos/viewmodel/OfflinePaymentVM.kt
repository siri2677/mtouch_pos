package com.example.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.cardreader.CardReaderData
import com.example.domain.model.cardreader.CardReaderStatus
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.OfflinePaymentPushData
import com.example.domain.model.payment.VanData
import com.example.domain.model.user.UserDetailData
import com.example.domain.usecase.cardreader.CommunicateKsnetCardReader
import com.example.domain.usecase.cardreader.FetchConnectedDeviceInfo
import com.example.domain.usecase.offlinePayment.KsnetSocketCommunicate
import com.example.domain.usecase.offlinePayment.PushOfflinePayment
import com.example.domain.usecase.offlinePayment.RequestOfflinePayment
import com.example.domain.usecase.user.FetchConnectedUserInfo
import com.example.domain.usecase.user.FetchSavedUserInfo
import com.example.mtouchpos.intent.CardTerminalCommunicateManager
import com.example.mtouchpos.viewmodel.mapper.toApprovePaymentData
import com.example.mtouchpos.viewmodel.mapper.toCancelPaymentData
import com.example.mtouchpos.viewmodel.mapper.toCompletePaymentInfo
import com.example.mtouchpos.viewmodel.mapper.toUserInfo
import com.example.mtouchpos.vo.info.ApprovedPaymentType
import com.example.mtouchpos.vo.info.PaymentProcessState
import com.example.mtouchpos.vo.type.DeviceType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject
import kotlin.collections.map

@HiltViewModel
class OfflinePaymentVM @Inject constructor(
    private val fetchConnectedDeviceInfoUseCase: FetchConnectedDeviceInfo,
    private val fetchConnectedUserInfo: FetchConnectedUserInfo,
    private val requestOfflinePaymentUseCase: RequestOfflinePayment,
    private val communicateKsnetCardReader: CommunicateKsnetCardReader,
    private val socketCommunicateVan: KsnetSocketCommunicate,
    private val pushOfflinePaymentUseCase: PushOfflinePayment
) : ViewModel(), Serializable {
    companion object {
        const val INSERT_IC_CARD = "카드를 IC 슬롯에 넣어 주세요"
        const val PROCESS_PAYMENT = "결제 진행 중 입니다\n잠시만 기다려 주세요"
        const val EVENT_FALLBACK = "[FallBack 거래발생]\n마그네틱으로 카드를 읽혀주세요"
    }

    sealed interface OfflinePaymentInfo: Serializable{
        val merchantUrl: String?
        val dptId: String?
        val totalAmount: String
        val freeAmount: Int
        val serviceAmount: Int
        val trackId: String?

        data class Approve(
            override val totalAmount: String = "",
            override val merchantUrl: String? = null,
            override val dptId: String? = null,
            override val trackId: String? = null,
            override val freeAmount: Int = 0,
            override val serviceAmount: Int = 0,
            val installment: String = "일시불",
        ): OfflinePaymentInfo

        data class Cancel(
            override val totalAmount: String = "",
            override val merchantUrl: String? = null,
            override val dptId: String? = null,
            override val trackId: String? = null,
            override val freeAmount: Int = 0,
            override val serviceAmount: Int = 0,
            val installment: String = "",
            val rootTrxId: String? = "",
            val authCode: String = "",
            val authDate: String = ""
        ): OfflinePaymentInfo
    }

    private val _paymentProcessState = MutableStateFlow<PaymentProcessState>(PaymentProcessState.Init)
    val paymentProcessState = _paymentProcessState.asStateFlow()

    private val _offlinePaymentInfo: MutableStateFlow<OfflinePaymentInfo> = MutableStateFlow(OfflinePaymentInfo.Approve())
    val offlinePaymentInfo = _offlinePaymentInfo.asStateFlow()

    fun processInit() {
        _paymentProcessState.value = PaymentProcessState.Init
    }

    fun stopRetry() { communicateKsnetCardReader.init() }

    fun getConnectedUserInfo() = fetchConnectedUserInfo()?.toUserInfo()

    fun getCurrentCardReaderData() = when(val cardReader = fetchConnectedDeviceInfoUseCase.getCurrentCardReaderData()) {
        is CardReaderData.Bluetooth -> DeviceType.Bluetooth(cardReader.deviceInformation)
        is CardReaderData.Usb -> DeviceType.Usb(cardReader.deviceInformation)
        is CardReaderData.Init -> null
    }

    fun requestOfflinePayment(communicateCardTerminal: CardTerminalCommunicateManager? = null) {
        viewModelScope.launch {
            flow {
                val paymentData = when (val info = _offlinePaymentInfo.value) {
                    is OfflinePaymentInfo.Cancel -> info.toCancelPaymentData()
                    is OfflinePaymentInfo.Approve -> info.toApprovePaymentData()
                }

                emit(PaymentProcessState.Loading)

                _offlinePaymentInfo.value.dptId?.let {
                    processTerminalOrReader(
                        communicateCardTerminal = communicateCardTerminal,
                        paymentData = paymentData,
                        vanData = VanData(dptId = it),
                        deviceInfo = fetchConnectedDeviceInfoUseCase.getCurrentCardReaderData().deviceInformation
                    )
                } ?: run {
                    handleRequestPayment(
                        communicateCardTerminal = communicateCardTerminal,
                        paymentData = paymentData,
                        deviceInfo = fetchConnectedDeviceInfoUseCase.getCurrentCardReaderData().deviceInformation
                    )
                }
            }.collect {
                _paymentProcessState.emit(it)
            }
        }
    }

    suspend fun FlowCollector<PaymentProcessState>.handleRequestPayment(
        communicateCardTerminal: CardTerminalCommunicateManager?,
        paymentData: OfflinePaymentData,
        deviceInfo: String
    ) {
        requestOfflinePaymentUseCase(paymentData).collect { paymentProcessState ->
            when(paymentProcessState) {
                is ApiResult.Error -> emit(PaymentProcessState.Error(paymentProcessState.message))
                is ApiResult.Exception -> emit(PaymentProcessState.Error(paymentProcessState.exception.toString()))
                is ApiResult.Success -> {
                    emit(PaymentProcessState.Approve(paymentProcessState.value.vanTrackId!!))
                    processTerminalOrReader(communicateCardTerminal, paymentProcessState.value, paymentData, deviceInfo)
                }
            }
        }
    }

    suspend fun FlowCollector<PaymentProcessState>.processTerminalOrReader(
        communicateCardTerminal: CardTerminalCommunicateManager?,
        vanData: VanData,
        paymentData: OfflinePaymentData,
        deviceInfo: String
    ) {
        communicateCardTerminal?.let { cardTerminal ->
            cardTerminal(
                paymentInfo = paymentData,
                dptId = vanData.dptId
            )?.let { errorMessage ->
                emit(PaymentProcessState.Error(errorMessage))
            }
        } ?: handleCardReader(vanData, paymentData, deviceInfo)
    }

    suspend fun FlowCollector<PaymentProcessState>.handleCardReader(
        vanData: VanData,
        paymentData: OfflinePaymentData,
        deviceInfo: String
    ) {
        communicateKsnetCardReader(paymentData, deviceInfo).onEach {
            if(it is CardReaderStatus.Communication.result) {
                processVanCommunication(vanData, it, paymentData)
            }
        }.map {
            when(it) {
                is CardReaderStatus.Communication.FallBack -> PaymentProcessState.CommunicateCardReader.Fallback(it.description)
                CardReaderStatus.Communication.InsertIC -> PaymentProcessState.CommunicateCardReader.InsertIC
                CardReaderStatus.Communication.ReadingIC -> PaymentProcessState.CommunicateCardReader.ReadingIC
                is CardReaderStatus.Connection.Establishing -> PaymentProcessState.CommunicateCardReader.Connecting(it.attempts)
                else -> null
            }
        }.collect { paymentProcessState ->
            paymentProcessState?.let { emit(it) }
        }
    }

    suspend fun FlowCollector<PaymentProcessState>.processVanCommunication(
        vanData: VanData,
        serialResult: CardReaderStatus.Communication.result,
        paymentData: OfflinePaymentData
    ) {
        socketCommunicateVan(
            offlinePaymentData = paymentData,
            paymentVanInfo = vanData.copy(),
            resultCommunicateData = serialResult
        ).map {
            when(it) {
                is ApiResult.Error -> PaymentProcessState.Error(it.message)
                is ApiResult.Exception -> PaymentProcessState.Error(it.exception.toString())
                is ApiResult.Success -> PaymentProcessState.Complete(it.value.toCompletePaymentInfo(fetchConnectedUserInfo()?.vat))
            }
        }.collect { paymentProcessState ->
            emit(paymentProcessState)
        }
    }

    fun pushOfflinePayment(
        completePaymentViewInfo: ApprovedPaymentType.CompletePaymentViewInfo
    ) {
        viewModelScope.launch {
            val vanTrxId = (paymentProcessState.value as PaymentProcessState.Approve).vanTrackId
            when(val offlinePaymentInfo = _offlinePaymentInfo.value) {
                is OfflinePaymentInfo.Cancel -> {
                    OfflinePaymentPushData(
                        amount = completePaymentViewInfo.totalAmount,
                        trackId = offlinePaymentInfo.trackId,
                        vanTrxId = vanTrxId,
                        installment = completePaymentViewInfo.installment,
                        authCode = completePaymentViewInfo.authCode,
                        authDate = completePaymentViewInfo.authDate,
                        cardNumber = completePaymentViewInfo.cardNumber,
                        rootTrxId = offlinePaymentInfo.rootTrxId
                    )
                }

                is OfflinePaymentInfo.Approve -> {
                    OfflinePaymentPushData(
                        amount = completePaymentViewInfo.totalAmount,
                        trackId = offlinePaymentInfo.trackId,
                        vanTrxId = vanTrxId,
                        installment = completePaymentViewInfo.installment,
                        authCode = completePaymentViewInfo.authCode,
                        authDate = completePaymentViewInfo.authDate,
                        cardNumber = completePaymentViewInfo.cardNumber,
                        rootTrxId = null
                    )
                }
            }.let {
                pushOfflinePaymentUseCase(it)
            }.map { apiResult ->
                when(apiResult) {
                    is ApiResult.Error -> PaymentProcessState.Error(apiResult.message)
                    is ApiResult.Exception -> PaymentProcessState.Error(apiResult.exception.toString())
                    is ApiResult.Success -> PaymentProcessState.Complete(
                        apiResult.value.toCompletePaymentInfo(fetchConnectedUserInfo()?.vat).copy(
                            issuer = completePaymentViewInfo.issuer,
                            acquirer = completePaymentViewInfo.acquirer
                        )
                    )
                }
            }.collect {
                _paymentProcessState.emit(it)
            }
        }
    }

    fun updateOfflinePaymentInfo(offlinePaymentInfo: OfflinePaymentInfo) {
        val vat = fetchConnectedUserInfo()?.vat

        _paymentProcessState.value = PaymentProcessState.Init
        _offlinePaymentInfo.value = when(offlinePaymentInfo) {
            is OfflinePaymentInfo.Approve -> {
                offlinePaymentInfo.copy(
                    freeAmount = if(vat == null || vat == "Y") offlinePaymentInfo.freeAmount
                        else offlinePaymentInfo.totalAmount.toInt() - offlinePaymentInfo.serviceAmount,
                )
            }
            is OfflinePaymentInfo.Cancel -> {
                offlinePaymentInfo.copy(
                    freeAmount = if(vat == null || vat == "Y") offlinePaymentInfo.freeAmount
                        else offlinePaymentInfo.totalAmount.toInt() - offlinePaymentInfo.serviceAmount,
                )
            }
        }
    }
}