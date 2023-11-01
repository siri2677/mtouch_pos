package com.example.cleanarchitech_text_0506.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.dto.request.tms.RequestGetPaymentListDto
import com.example.domain.repositoryInterface.GetPaymentInformationRepository
import com.example.domain.repositoryInterface.UserInformationSharedPreference
import com.example.domain.sealed.ResponsePayAPI
import com.example.domain.sealed.ResponseTmsAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    private val getPaymentInformationRepository: GetPaymentInformationRepository,
    private val userInformationSharedPreference: UserInformationSharedPreference
): ViewModel() {
    private val _responseTmsAPI = MutableSharedFlow<ResponseTmsAPI>()
    val responseTmsAPI = _responseTmsAPI.asSharedFlow()

    fun getPaymentList(requestGetPaymentListDto: RequestGetPaymentListDto) {
        getPaymentInformationRepository.list(
            onSuccess = { viewModelScope.launch{ _responseTmsAPI.emit(ResponseTmsAPI.GetPaymentList(it)) } },
            onError = { viewModelScope.launch{ _responseTmsAPI.emit(ResponseTmsAPI.ErrorMessage(it)) } },
            token = userInformationSharedPreference.getUserInformation().key,
            body = requestGetPaymentListDto
        )
    }
}