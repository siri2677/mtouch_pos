package com.example.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.device.manager.CommunicateCardTerminalManager
import com.example.domain.usecase.device.FetchConnectedDeviceInfo
import com.example.domain.usecase.offlinePayment.OfflineCancelPayment
import com.example.domain.usecase.offlinePayment.OfflinePayment
import com.example.mtouchpos.viewmodel.mapper.toPaymentData
import com.example.mtouchpos.viewmodel.mapper.toPaymentProcessState
import com.example.mtouchpos.viewmodel.mapper.toRootPaymentData
import com.example.mtouchpos.vo.data.CompletePaymentInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class OfflinePaymentViewModel @Inject constructor(
    private val fetchConnectedDeviceInfoUseCase: FetchConnectedDeviceInfo,
    private val offlinePaymentUseCase: OfflinePayment,
    private val offlineCancelPaymentUseCase: OfflineCancelPayment,
//    private val deviceCommunicateManager: DeviceCommunicateManager
) : ViewModel(), Serializable {
    companion object {
        const val INSERT_IC_CARD = "카드를 IC 슬롯에 넣어 주세요"
        const val PROCESS_PAYMENT = "결제 진행 중 입니다.\n 잠시만 기다려 주세요"
        const val EVENT_FALLBACK = "FallBack 거래발생. 마그네틱으로 카드를 읽혀주세요"
    }

    data class OfflinePaymentInfo(
        val installment: String = "00",
        val totalAmount: Int = 0,
        val trackId: String = "AXD_${Date().time}",
        val freeAmount: Int = 0,
        val serviceAmount: Int = 0
    )

    data class OfflineCancelPaymentInfo(
        val amount: String = "",
        val installment: String = "",
        val trackId: String = "",
        val trxId: String = "",
        val authCode: String = "",
        val authDate: String = "",
        val freeAmount: Int = 0,
        val serviceAmount: Int = 0
    )

    sealed interface PaymentProcessState: Serializable {
        data object Init: PaymentProcessState
        data class Error(val message: String): PaymentProcessState
        data object InsertIC: PaymentProcessState
        data object ReadingIC: PaymentProcessState
        data class Fallback(val message: String): PaymentProcessState
        data class CompletePayment(val data: CompletePaymentInfo): PaymentProcessState
    }

    private val _paymentProcessState = MutableSharedFlow<PaymentProcessState>()
    val paymentProcessState = _paymentProcessState.asSharedFlow()

    private val _offlinePaymentInfo = MutableStateFlow(OfflinePaymentInfo())
    val offlinePaymentInfo = _offlinePaymentInfo.asStateFlow()

    private val _offlineCancelPaymentInfo = MutableStateFlow(OfflineCancelPaymentInfo())
    val offlineCancelPaymentInfo = _offlineCancelPaymentInfo.asStateFlow()

    fun updateOfflinePaymentInfo(offlinePaymentInfo: OfflinePaymentInfo) {
        _offlinePaymentInfo.value = offlinePaymentInfo
    }

    fun updateOfflineCancelPaymentInfo(offlineCancelPaymentInfo: OfflineCancelPaymentInfo) {
        _offlineCancelPaymentInfo.value = offlineCancelPaymentInfo
    }

    fun fetchConnectedDeviceInfo() = fetchConnectedDeviceInfoUseCase()

    fun requestOfflinePayment(communicateCardTerminal: CommunicateCardTerminalManager?) {
        viewModelScope.launch {
            offlinePaymentUseCase(
                offlinePaymentInfo.value.toPaymentData(),
                communicateCardTerminal
            ).map {
                it.toPaymentProcessState()
            }.collect {
                _paymentProcessState.emit(it)
            }
        }
    }

    fun requestOfflineCancelPayment(communicateCardTerminal: CommunicateCardTerminalManager?) {
        viewModelScope.launch {
            offlineCancelPaymentUseCase(
                offlineCancelPaymentInfo.value.toPaymentData(),
                offlineCancelPaymentInfo.value.toRootPaymentData(),
                communicateCardTerminal
            ).map {
                it.toPaymentProcessState()
            }.collect {
                _paymentProcessState.emit(it)
            }
        }
    }
}