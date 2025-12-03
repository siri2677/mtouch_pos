package com.kwonps.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwonps.domain.usecase.directPayment.RequestDirectCancelPayment
import com.kwonps.domain.usecase.directPayment.RequestDirectPayment
import com.kwonps.domain.usecase.user.FetchConnectedUserInfo
import com.kwonps.mtouchpos.viewmodel.mapper.toApproveDirectPaymentData
import com.kwonps.mtouchpos.viewmodel.mapper.toCancelDirectPaymentData
import com.kwonps.mtouchpos.viewmodel.mapper.toCompletePaymentInfo
import com.kwonps.mtouchpos.viewmodel.mapper.toDirectCancelPaymentInfo
import com.kwonps.mtouchpos.viewmodel.mapper.toUseCaseResult
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType
import com.kwonps.mtouchpos.vo.type.UseCaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DirectPaymentVM @Inject constructor(
    private val fetchConnectedUserInfo: FetchConnectedUserInfo,
    private val directPayment: RequestDirectPayment,
    private val directCancelPayment: RequestDirectCancelPayment
) : ViewModel() {
    data class DirectPaymentInfo(
        val amount: String = "",
        val installment: String = "00",
        val trackId: String = "AXD_${Date().time}",
        val productName: String = "",
        val cardNumber: String = "",
        val expirationYear: String = Calendar.getInstance().get(Calendar.YEAR).toString(),
        val expirationMonth: String = Calendar.getInstance().get(Calendar.MONTH).toString(),
        val expiry: String = "",
        val cardAuth: String = "",
        val payerName: String = "",
        val payerTel: String = "",
        val authPw: String? = "",
        val authDob: String? = ""
    )

    data class DirectCancelPaymentInfo(
        val amount: String = "",
        val installment: String = "",
        val cardNumber: String = "",
        val trackId: String = "AXD_${Date().time}",
        val trxId: String = "",
    )

    data class DirectPaymentUiState(
        val directPaymentInfo: DirectPaymentInfo = DirectPaymentInfo(),
        val directCancelPaymentInfo: DirectCancelPaymentInfo = DirectCancelPaymentInfo(),
        val paymentState: DirectPaymentState = DirectPaymentState.Idle
    )

    sealed interface DirectPaymentState {
        object Idle : DirectPaymentState
        object Loading : DirectPaymentState
        data class Completed(val data: ApprovedPaymentType.CompletePaymentViewInfo) : DirectPaymentState
        data class Failed(val message: String) : DirectPaymentState
    }

    sealed interface DirectPaymentUiEvent {
        data class NavigateToComplete(val data: ApprovedPaymentType.CompletePaymentViewInfo) : DirectPaymentUiEvent
    }

    private val _uiState = MutableStateFlow(DirectPaymentUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DirectPaymentUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private fun updatePaymentState(paymentState: DirectPaymentState) {
        _uiState.update { it.copy(paymentState = paymentState) }
    }

    private fun processDirectPayment(directPaymentViewInfo: DirectPaymentInfo) {
        viewModelScope.launch {
            updatePaymentState(DirectPaymentState.Loading)
            directPayment(
                directPaymentViewInfo.toApproveDirectPaymentData(),
            ).map { apiResult ->
                apiResult.toUseCaseResult { it.toCompletePaymentInfo(fetchConnectedUserInfo()?.vat) }
            }.collect {
                reducePaymentResult(it)
            }
        }
    }

    private fun processDirectCancelPayment(directCancelPaymentInfo: DirectCancelPaymentInfo) {
        viewModelScope.launch {
            updatePaymentState(DirectPaymentState.Loading)
            directCancelPayment(
                directCancelPaymentInfo.toCancelDirectPaymentData(),
            ).map { apiResult ->
                apiResult.toUseCaseResult { it.toCompletePaymentInfo(fetchConnectedUserInfo()?.vat) }
            }.collect {
                reducePaymentResult(it)
            }
        }
    }

    fun updateDirectPaymentInfo(directPaymentViewInfo: DirectPaymentInfo) {
        _uiState.update { it.copy(directPaymentInfo = directPaymentViewInfo) }
    }

    fun requestDirectPayment() {
        processDirectPayment(uiState.value.directPaymentInfo)
    }


    fun requestDirectCancelPayment(completePaymentViewInfo: ApprovedPaymentType.CompletePaymentViewInfo) {
        processDirectCancelPayment(completePaymentViewInfo.toDirectCancelPaymentInfo())
    }

    fun requestDirectCancelPayment(directCancelPaymentInfo: DirectCancelPaymentInfo) {
        processDirectCancelPayment(directCancelPaymentInfo)
    }

    fun dismissDialog() {
        updatePaymentState(DirectPaymentState.Idle)
    }

    private suspend fun reducePaymentResult(result: UseCaseResult<ApprovedPaymentType.CompletePaymentViewInfo>) {
        when (result) {
            is UseCaseResult.Success -> {
                updatePaymentState(DirectPaymentState.Completed(result.value))
                _uiEvent.emit(DirectPaymentUiEvent.NavigateToComplete(result.value))
            }

            is UseCaseResult.Error -> updatePaymentState(DirectPaymentState.Failed(result.message))
            is UseCaseResult.Exception -> updatePaymentState(
                DirectPaymentState.Failed(result.exception.message ?: "결제 중 오류가 발생했습니다.")
            )

            is UseCaseResult.Loading -> updatePaymentState(DirectPaymentState.Loading)
            else -> updatePaymentState(DirectPaymentState.Idle)
        }
    }
}
