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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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

    private val _reactDirectPaymentInfo =
        MutableStateFlow<UseCaseResult<ApprovedPaymentType.CompletePaymentViewInfo>>(UseCaseResult.Init)
    val reactDirectPaymentInfo = _reactDirectPaymentInfo.asStateFlow()

    private val _directPaymentInfo = MutableStateFlow(DirectPaymentInfo())
    val directPaymentInfo = _directPaymentInfo.asStateFlow()

    private val _directCancelPaymentInfo = MutableStateFlow(DirectCancelPaymentInfo())
    val directCancelPaymentInfo = _directCancelPaymentInfo.asStateFlow()


    private fun processDirectPayment(directPaymentViewInfo: DirectPaymentInfo) {
        viewModelScope.launch {
            _reactDirectPaymentInfo.value = UseCaseResult.Loading
            directPayment(
                directPaymentViewInfo.toApproveDirectPaymentData()
            ).map { apiResult ->
                apiResult.toUseCaseResult { it.toCompletePaymentInfo(fetchConnectedUserInfo()?.vat) }
            }.collect {
                _reactDirectPaymentInfo.emit(it)
            }
        }
    }

    private fun processDirectCancelPayment(directCancelPaymentInfo: DirectCancelPaymentInfo) {
        viewModelScope.launch {
            _reactDirectPaymentInfo.value = UseCaseResult.Loading
            directCancelPayment(
                directCancelPaymentInfo.toCancelDirectPaymentData()
            ).map { apiResult ->
                apiResult.toUseCaseResult { it.toCompletePaymentInfo(fetchConnectedUserInfo()?.vat) }
            }.collect {
                _reactDirectPaymentInfo.emit(it)
            }
        }
    }

    fun updateDirectPaymentInfo(directPaymentViewInfo: DirectPaymentInfo) {
        _directPaymentInfo.value = directPaymentViewInfo
    }

    fun requestDirectPayment() {
        processDirectPayment(directPaymentInfo.value)
    }


    fun requestDirectCancelPayment(completePaymentViewInfo: ApprovedPaymentType.CompletePaymentViewInfo) {
        processDirectCancelPayment(completePaymentViewInfo.toDirectCancelPaymentInfo())
    }

    fun requestDirectCancelPayment(directCancelPaymentInfo: DirectCancelPaymentInfo) {
        processDirectCancelPayment(directCancelPaymentInfo)
    }
}