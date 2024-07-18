package com.example.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.directPayment.DirectCancelPayment
import com.example.domain.usecase.directPayment.DirectPayment
import com.example.mtouchpos.viewmodel.mapper.toCardData
import com.example.mtouchpos.viewmodel.mapper.toCompletePaymentInfo
import com.example.mtouchpos.viewmodel.mapper.toDirectPaymentData
import com.example.mtouchpos.viewmodel.mapper.toPaymentData
import com.example.mtouchpos.viewmodel.mapper.toRootPaymentData
import com.example.mtouchpos.viewmodel.mapper.toUseCaseResult
import com.example.mtouchpos.vo.data.CompletePaymentInfo
import com.example.mtouchpos.vo.type.TransactionType
import com.example.mtouchpos.vo.type.UseCaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DirectPaymentViewModel @Inject constructor(
    private val directPayment: DirectPayment,
    private val directCancelPayment: DirectCancelPayment
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
        val trackId: String = "",
        val trxId: String = "",
        val authCode: String = "",
        val authDate: String = "",
        val issuer: String = "",
        val cardNumber: String = ""
    )

    private val _reactDirectPaymentInfo = MutableSharedFlow<UseCaseResult<CompletePaymentInfo>>()
    val reactDirectPaymentInfo = _reactDirectPaymentInfo.asSharedFlow()

    private val _directPaymentInfo = MutableStateFlow(DirectPaymentInfo())
    val directPaymentInfo = _directPaymentInfo.asStateFlow()

    private val _directCancelPaymentInfo = MutableStateFlow(DirectCancelPaymentInfo())
    val directCancelPaymentInfo = _directCancelPaymentInfo.asStateFlow()

    fun updateDirectPaymentInfo(directPaymentViewInfo: DirectPaymentInfo) {
        _directPaymentInfo.value = directPaymentViewInfo
    }

    fun updateDirectCancelPaymentInfo(directCancelPaymentInfo: DirectCancelPaymentInfo) {
        _directCancelPaymentInfo.value = directCancelPaymentInfo
    }

    fun requestDirectPayment() {
        viewModelScope.launch {
            directPayment(
                paymentInfo = directPaymentInfo.value.toPaymentData(),
                directPaymentInfo = directPaymentInfo.value.toDirectPaymentData()
            ).map { apiResult ->
                apiResult.toUseCaseResult { it.toCompletePaymentInfo(TransactionType.DIRECT) }
            }.collect {
                _reactDirectPaymentInfo.emit(it)
            }
        }
    }

    fun requestDirectCancelPayment() {
        viewModelScope.launch {
            directCancelPayment(
                paymentInfo = directCancelPaymentInfo.value.toPaymentData(),
                rootPaymentInfo = directCancelPaymentInfo.value.toRootPaymentData(),
                cardInfo = directCancelPaymentInfo.value.toCardData(),
            ).map { apiResult ->
                apiResult.toUseCaseResult { it.toCompletePaymentInfo(TransactionType.DIRECT) }
            }.collect {
                _reactDirectPaymentInfo.emit(it)
            }
        }
    }
}