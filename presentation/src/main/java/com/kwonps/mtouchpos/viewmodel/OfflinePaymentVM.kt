package com.kwonps.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwonps.domain.model.cardreader.CardReaderData
import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.model.payment.AmountData
import com.kwonps.domain.model.payment.Installment
import com.kwonps.domain.model.payment.OfflinePaymentData
import com.kwonps.domain.model.payment.OfflinePaymentPushData
import com.kwonps.domain.model.payment.PaymentError
import com.kwonps.domain.model.payment.PaymentResult
import com.kwonps.domain.model.payment.ReceiptIdentifiers
import com.kwonps.domain.model.payment.VanData
import com.kwonps.domain.usecase.cardreader.CommunicateKsnetCardReader
import com.kwonps.domain.usecase.cardreader.FetchConnectedDeviceInfo
import com.kwonps.domain.usecase.cardreader.PrintCompletedTransaction
import com.kwonps.domain.usecase.offlinePayment.ProcessOfflinePayment
import com.kwonps.domain.usecase.offlinePayment.RequestOfflinePayment
import com.kwonps.domain.usecase.offlinePayment.SyncReceipt
import com.kwonps.domain.usecase.user.FetchConnectedUserInfo
import com.kwonps.mtouchpos.intent.CardTerminalCommunicateManager
import com.kwonps.mtouchpos.viewmodel.mapper.toApprovePaymentData
import com.kwonps.mtouchpos.viewmodel.mapper.toCancelPaymentData
import com.kwonps.mtouchpos.viewmodel.mapper.toCompletePaymentInfo
import com.kwonps.mtouchpos.viewmodel.mapper.toPaymentDetailData
import com.kwonps.mtouchpos.viewmodel.mapper.toUserInfo
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType
import com.kwonps.mtouchpos.vo.info.PaymentProcessState
import com.kwonps.mtouchpos.vo.type.DeviceType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class OfflinePaymentVM @Inject constructor(
    private val fetchConnectedDeviceInfoUseCase: FetchConnectedDeviceInfo,
    private val fetchConnectedUserInfo: FetchConnectedUserInfo,
    private val requestOfflinePaymentUseCase: RequestOfflinePayment,
    private val communicateKsnetCardReader: CommunicateKsnetCardReader,
    private val processOfflinePayment: ProcessOfflinePayment,
    private val syncReceipt: SyncReceipt,
    private val printCompletedTransaction: PrintCompletedTransaction
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
                is PaymentResult.Failure -> emit(PaymentProcessState.Error(mapError(paymentProcessState.error)))
                is PaymentResult.Success -> {
                    paymentProcessState.value.vanTrackId?.let { emit(PaymentProcessState.Approve(it)) }
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
        processOfflinePayment(
            ProcessOfflinePayment.Input(
                payment = paymentData,
                communicationResult = serialResult,
                presetVanData = vanData.copy()
            )
        ).map {
            when(it) {
                is PaymentResult.Failure -> PaymentProcessState.Error(mapError(it.error))
                is PaymentResult.Success -> PaymentProcessState.Complete(it.value.paymentDetail.toCompletePaymentInfo(fetchConnectedUserInfo()?.vat))
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
            val pushData = when(val offlinePaymentInfo = _offlinePaymentInfo.value) {
                is OfflinePaymentInfo.Cancel -> {
                    OfflinePaymentPushData(
                        amount = completePaymentViewInfo.toAmountData(),
                        installment = Installment(completePaymentViewInfo.installment),
                        identifiers = ReceiptIdentifiers(
                            vanTrxId = vanTrxId,
                            rootTrxId = offlinePaymentInfo.rootTrxId,
                            trackId = offlinePaymentInfo.trackId
                        ),
                        approval = com.kwonps.domain.model.payment.PaymentDetailData.ApprovalInfo(
                            authCode = completePaymentViewInfo.authCode,
                            authDate = completePaymentViewInfo.authDate
                        ),
                        cardNumber = completePaymentViewInfo.cardNumber
                    )
                }

                is OfflinePaymentInfo.Approve -> {
                    OfflinePaymentPushData(
                        amount = completePaymentViewInfo.toAmountData(),
                        installment = Installment(completePaymentViewInfo.installment),
                        identifiers = ReceiptIdentifiers(
                            vanTrxId = vanTrxId,
                            rootTrxId = null,
                            trackId = offlinePaymentInfo.trackId
                        ),
                        approval = com.kwonps.domain.model.payment.PaymentDetailData.ApprovalInfo(
                            authCode = completePaymentViewInfo.authCode,
                            authDate = completePaymentViewInfo.authDate
                        ),
                        cardNumber = completePaymentViewInfo.cardNumber
                    )
                }
            }

            syncReceipt(pushData).map { result ->
                when(result) {
                    is PaymentResult.Failure -> PaymentProcessState.Error(mapError(result.error))
                    is PaymentResult.Success -> PaymentProcessState.Complete(
                        result.value.toCompletePaymentInfo(fetchConnectedUserInfo()?.vat).copy(
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

    fun print(completePaymentViewInfo: ApprovedPaymentType.CompletePaymentViewInfo) {
        viewModelScope.launch {
            printCompletedTransaction(
                deviceInformation = fetchConnectedDeviceInfoUseCase.getCurrentCardReaderData().deviceInformation,
                userDetailData = fetchConnectedUserInfo()!!,
                paymentDetailData = completePaymentViewInfo.toPaymentDetailData()
            ).collect {}
        }
    }

    private fun mapError(error: PaymentError): String = when(error) {
        is PaymentError.Communication -> error.message
        is PaymentError.Conflict -> error.reason
        is PaymentError.Validation -> error.reason
        is PaymentError.Unknown -> error.throwable.message ?: "알 수 없는 오류가 발생했습니다."
    }

    private fun ApprovedPaymentType.CompletePaymentViewInfo.toAmountData() = AmountData(
        totalAmount = totalAmount.toIntOrZero(),
        freeAmount = freeAmount?.toIntOrZero() ?: 0,
        serviceAmount = serviceAmount?.toIntOrZero() ?: 0
    )

    private fun String.toIntOrZero(): Int = toIntOrNull() ?: 0
}