package com.example.mtouchpos.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.OfflinePaymentPushData
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.usecase.device.FetchConnectedDeviceInfo
import com.example.domain.usecase.device.manager.CommunicateCardTerminalManager
import com.example.domain.usecase.offlinePayment.PushOfflineCancelPayment
import com.example.domain.usecase.offlinePayment.PushOfflinePayment
import com.example.domain.usecase.offlinePayment.RequestOfflineCancelPayment
import com.example.domain.usecase.offlinePayment.RequestOfflinePayment
import com.example.mtouchpos.viewmodel.mapper.toApprovePaymentData
import com.example.mtouchpos.viewmodel.mapper.toCancelPaymentData
import com.example.mtouchpos.viewmodel.mapper.toPaymentProcessState
import com.example.mtouchpos.vo.data.ApprovedPaymentType
import com.example.mtouchpos.vo.type.PurchaseType
import com.example.mtouchpos.vo.type.UseCaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class OfflinePaymentViewModel @Inject constructor(
    private val fetchConnectedDeviceInfoUseCase: FetchConnectedDeviceInfo,
    private val offlinePaymentUseCase: RequestOfflinePayment,
    private val offlineCancelPaymentUseCase: RequestOfflineCancelPayment,
    private val pushOfflinePaymentUseCase: PushOfflinePayment,
    private val pushOfflineCancelPaymentUseCase: PushOfflineCancelPayment
) : ViewModel(), Serializable {
    companion object {
        const val SINGLE_INSTANCE = "SINGLE_INSTANCE"
        const val INSERT_IC_CARD = "카드를 IC 슬롯에 넣어 주세요"
        const val PROCESS_PAYMENT = "결제 진행 중 입니다.\n 잠시만 기다려 주세요"
        const val EVENT_FALLBACK = "FallBack 거래발생. 마그네틱으로 카드를 읽혀주세요"
    }

    sealed interface OfflinePaymentInfo: Serializable{
        data class Approve(
            val installment: String = "일시불",
            val totalAmount: Int = 0,
            val trackId: String? = null,
            val freeAmount: Int = 0,
            val serviceAmount: Int = 0
        ): OfflinePaymentInfo

        data class Cancel(
            val amount: String = "",
            val installment: String = "",
            val trackId: String? = null,
            val rootTrxId: String = "",
            val authCode: String = "",
            val authDate: String = "",
            val freeAmount: Int = 0,
            val serviceAmount: Int = 0
        ): OfflinePaymentInfo
    }

    sealed interface PaymentProcessState: Serializable {
        data object Init: PaymentProcessState
        data object Loading: PaymentProcessState
        data class Error(val message: String): PaymentProcessState
        data object InsertIC: PaymentProcessState
        data object ReadingIC: PaymentProcessState
        data class Fallback(val message: String): PaymentProcessState
        data class CompletePayment(val data: ApprovedPaymentType.CompletePaymentViewInfo): PaymentProcessState
    }

    private val _paymentProcessState = MutableSharedFlow<PaymentProcessState>()
    val paymentProcessState = _paymentProcessState.asSharedFlow()

    private val _offlinePaymentInfo = MutableStateFlow(OfflinePaymentInfo.Approve())
    val offlinePaymentInfo = _offlinePaymentInfo.asStateFlow()

    private val _offlineCancelPaymentInfo = MutableStateFlow(OfflinePaymentInfo.Cancel())
    val offlineCancelPaymentInfo = _offlineCancelPaymentInfo.asStateFlow()

    var vanTrxId: String? = null
    var processExit: ((String) -> Unit)? = null

    fun requestOfflinePayment(communicateCardTerminal: CommunicateCardTerminalManager? = null) {
        viewModelScope.launch {
            _paymentProcessState.emit(PaymentProcessState.Loading)
            offlinePaymentUseCase(
                _offlinePaymentInfo.value.toApprovePaymentData(),
                communicateCardTerminal
            ).onEach { apiResult ->
                if (apiResult is ApiResult.Success) {
                    (apiResult.value as? PaymentProcessStatus.ApprovePayment)?.let {
                        vanTrxId = it.trackId
                    }
                }
            }.map { apiResult ->
                apiResult.toPaymentProcessState()
            }.collect {
                _paymentProcessState.emit(it)
            }
        }
    }

    fun requestOfflineCancelPayment(communicateCardTerminal: CommunicateCardTerminalManager? = null) {
        viewModelScope.launch {
            _paymentProcessState.emit(PaymentProcessState.Loading)
            offlinePaymentUseCase(
                _offlineCancelPaymentInfo.value.toCancelPaymentData(),
                communicateCardTerminal
            ).onEach { apiResult ->
                if (apiResult is ApiResult.Success) {
                    (apiResult.value as? PaymentProcessStatus.ApprovePayment)?.let {
                        vanTrxId = it.trackId
                    }
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
        purchaseType: PurchaseType,
        authCode: String,
        authDate: String,
        cardNumber: String
    ) {
        viewModelScope.launch {
            _paymentProcessState.emit(PaymentProcessState.Loading)
            when(purchaseType) {
                PurchaseType.APPROVE -> {
                    pushOfflinePaymentUseCase(
                        OfflinePaymentPushData(
                            amount = amount ?: offlinePaymentInfo.value.totalAmount.toString(),
                            trackId = offlineCancelPaymentInfo.value.trackId,
                            vanTrxId = vanTrxId!!,
                            installment = installment,
                            authCode = authCode,
                            authDate = authDate,
                            cardNumber = cardNumber,
                            rootTrxId = null
                        )
                    )
                }

                PurchaseType.REFUND -> {
                    pushOfflineCancelPaymentUseCase(
                        OfflinePaymentPushData(
                            amount = amount ?: offlineCancelPaymentInfo.value.amount,
                            trackId = offlineCancelPaymentInfo.value.trackId,
                            vanTrxId = vanTrxId!!,
                            installment = installment,
                            authCode = authCode,
                            authDate = authDate,
                            cardNumber = cardNumber,
                            rootTrxId = offlineCancelPaymentInfo.value.rootTrxId
                        )
                    )
                }
            }.map { apiResult ->
                apiResult.toPaymentProcessState()
            }.collect {
                _paymentProcessState.emit(it)
            }
        }
    }

    fun fetchConnectedDeviceInfo() = fetchConnectedDeviceInfoUseCase()

    fun updateOfflinePaymentInfo(offlinePaymentInfo: OfflinePaymentInfo.Approve) {
        _offlinePaymentInfo.value = offlinePaymentInfo
    }

    fun updateOfflineCancelPaymentInfo(offlineCancelPaymentInfo: OfflinePaymentInfo.Cancel) {
        _offlineCancelPaymentInfo.value = offlineCancelPaymentInfo
    }
}