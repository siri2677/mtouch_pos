package com.example.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.payment.OfflinePaymentPushData
import com.example.domain.usecase.cardreader.FetchConnectedDeviceInfo
import com.example.domain.manager.cardterminal.CardTerminalCommunicateManager
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.usecase.offlinePayment.PushOfflineCancelPayment
import com.example.domain.usecase.offlinePayment.PushOfflinePayment
import com.example.domain.usecase.offlinePayment.RequestOfflineCancelPayment
import com.example.domain.usecase.offlinePayment.RequestOfflinePayment
import com.example.mtouchpos.viewmodel.mapper.toApprovePaymentData
import com.example.mtouchpos.viewmodel.mapper.toCancelPaymentData
import com.example.mtouchpos.viewmodel.mapper.toPaymentProcessState
import com.example.mtouchpos.vo.info.ApprovedPaymentType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class OfflinePaymentVM @Inject constructor(
    private val fetchConnectedDeviceInfoUseCase: FetchConnectedDeviceInfo,
    private val offlinePaymentUseCase: RequestOfflinePayment,
    private val offlineCancelPaymentUseCase: RequestOfflineCancelPayment,
    private val pushOfflinePaymentUseCase: PushOfflinePayment,
    private val pushOfflineCancelPaymentUseCase: PushOfflineCancelPayment
) : ViewModel(), Serializable {
    companion object {
        const val INSERT_IC_CARD = "카드를 IC 슬롯에 넣어 주세요"
        const val PROCESS_PAYMENT = "결제 진행 중 입니다\n잠시만 기다려 주세요"
        const val EVENT_FALLBACK = "[FallBack 거래발생]\n마그네틱으로 카드를 읽혀주세요"
    }

    sealed interface OfflinePaymentInfo: Serializable{
        val merchantUrl: String?

        data class Approve(
            val installment: String = "일시불",
            val totalAmount: Int = 0,
            val trackId: String? = null,
            val freeAmount: Int = 0,
            val serviceAmount: Int = 0,
            override val merchantUrl: String? = null
        ): OfflinePaymentInfo

        data class Cancel(
            val amount: String = "",
            val installment: String = "",
            val trackId: String? = null,
            val rootTrxId: String = "",
            val authCode: String = "",
            val authDate: String = "",
            val freeAmount: Int = 0,
            val serviceAmount: Int = 0,
            override val merchantUrl: String? = null
        ): OfflinePaymentInfo
    }

    sealed interface PaymentProcessState: Serializable {
        data object Init: PaymentProcessState
        data object Loading: PaymentProcessState
        data class Retry(val count: Int): PaymentProcessState
        data class Error(val message: String): PaymentProcessState
        data object InsertIC: PaymentProcessState
        data object ReadingIC: PaymentProcessState
        data class Fallback(val message: String): PaymentProcessState
        data class ApprovePayment(val vanTrxId: String): PaymentProcessState
        data class CompletePayment(val data: ApprovedPaymentType.CompletePaymentViewInfo): PaymentProcessState
    }

    private val _paymentProcessState = MutableStateFlow<PaymentProcessState>(PaymentProcessState.Init)
    val paymentProcessState = _paymentProcessState.asStateFlow()

    private val _offlinePaymentInfo: MutableStateFlow<OfflinePaymentInfo> = MutableStateFlow(OfflinePaymentInfo.Approve())
    val offlinePaymentInfo = _offlinePaymentInfo.asStateFlow()

    private val _offlineCancelPaymentInfo = MutableStateFlow(OfflinePaymentInfo.Cancel())
    val offlineCancelPaymentInfo = _offlineCancelPaymentInfo.asStateFlow()

    fun processInit() {
        _paymentProcessState.value = PaymentProcessState.Init
    }

//    fun requestOfflinePayment(communicateCardTerminal: CommunicateCardTerminalManager? = null) {
//        viewModelScope.launch {
//            _paymentProcessState.emit(PaymentProcessState.Loading)
//            offlinePaymentUseCase(
//                (_offlinePaymentInfo.value as OfflinePaymentInfo.Approve).toApprovePaymentData(),
//                communicateCardTerminal
//            ).onEach { apiResult ->
//                if (apiResult is ApiResult.Success) {
//                    (apiResult.value as? PaymentProcessStatus.ApprovePayment)?.let {
//                        vanTrxId = it.trackId
//                    }
//                }
//            }.map { apiResult ->
//                apiResult.toPaymentProcessState()
//            }.collect {
//                _paymentProcessState.emit(it)
//            }
//        }
//    }
    fun stopRetry() { offlinePaymentUseCase.stopRetry() }

    fun requestOfflinePayment(communicateCardTerminal: CardTerminalCommunicateManager? = null) {
        viewModelScope.launch {
            _paymentProcessState.emit(PaymentProcessState.Loading)
            when(val offlinePaymentInfo = _offlinePaymentInfo.value) {
                is OfflinePaymentInfo.Cancel -> {
                    offlinePaymentUseCase(
                        offlinePaymentInfo.toCancelPaymentData(),
                        communicateCardTerminal
                    )
                }
                is OfflinePaymentInfo.Approve -> {
                    offlinePaymentUseCase(
                        offlinePaymentInfo.toApprovePaymentData(),
                        communicateCardTerminal
                    )
                }
            }.map { apiResult ->
                apiResult.toPaymentProcessState()
            }.collect {
                _paymentProcessState.emit(it)
            }
        }
    }

    fun pushOfflinePayment(
        amount: String? = null,
        installment: String,
        authCode: String,
        authDate: String,
        cardNumber: String
    ) {
        viewModelScope.launch {
            val vanTrxId = (paymentProcessState.value as PaymentProcessState.ApprovePayment).vanTrxId
//            _paymentProcessState.emit(PaymentProcessState.Loading)
            when(val offlinePaymentInfo = _offlinePaymentInfo.value) {
                is OfflinePaymentInfo.Cancel -> {
                    OfflinePaymentPushData(
                        amount = amount ?: offlinePaymentInfo.amount,
                        trackId = offlinePaymentInfo.trackId,
                        vanTrxId = vanTrxId,
                        installment = installment,
                        authCode = authCode,
                        authDate = authDate,
                        cardNumber = cardNumber,
                        rootTrxId = offlinePaymentInfo.rootTrxId
                    )
                }

                is OfflinePaymentInfo.Approve -> {
                    OfflinePaymentPushData(
                        amount = amount ?: offlinePaymentInfo.totalAmount.toString(),
                        trackId = offlinePaymentInfo.trackId,
                        vanTrxId = vanTrxId,
                        installment = installment,
                        authCode = authCode,
                        authDate = authDate,
                        cardNumber = cardNumber,
                        rootTrxId = null
                    )
                }
            }.let {
                pushOfflinePaymentUseCase(it)
            }.map { apiResult ->
                apiResult.toPaymentProcessState()
            }.collect {
                _paymentProcessState.emit(it)
            }
        }
    }

    fun fetchConnectedDeviceInfo() = fetchConnectedDeviceInfoUseCase()

    fun updateOfflinePaymentInfo(offlinePaymentInfo: OfflinePaymentInfo.Approve) {
        _paymentProcessState.value = PaymentProcessState.Init
        _offlinePaymentInfo.value = offlinePaymentInfo
    }

    fun updateOfflineCancelPaymentInfo(offlineCancelPaymentInfo: OfflinePaymentInfo.Cancel) {
        _paymentProcessState.value = PaymentProcessState.Init
        _offlinePaymentInfo.value = offlineCancelPaymentInfo
    }
}