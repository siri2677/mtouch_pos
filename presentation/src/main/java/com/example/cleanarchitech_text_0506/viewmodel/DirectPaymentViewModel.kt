package com.example.cleanarchitech_text_0506.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.dto.request.pay.RequestDirectCancelPaymentDto
import com.example.domain.dto.request.pay.RequestDirectPaymentDto
import com.example.domain.repositoryInterface.DirectPaymentRepository
import com.example.domain.repositoryInterface.UserInformationRepository
import com.example.domain.repositoryInterface.UserInformationSharedPreference
import com.example.domain.sealed.ResponsePayAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DirectPaymentViewModel @Inject constructor(
    private val userInformationSharedPreference: UserInformationSharedPreference,
    private val userInformationRepository: UserInformationRepository,
    private val directPaymentRepository: DirectPaymentRepository
): ViewModel(){

    private val _responseDirectPayment = MutableSharedFlow<ResponsePayAPI>()
    val responseDirectPayment = _responseDirectPayment.asSharedFlow()

    fun requestDirectPayment(requestDirectPaymentDto: RequestDirectPaymentDto) {
        Log.w("requestDirectPayment", "requestDirectPayment")
        viewModelScope.launch {
            directPaymentRepository.approve(
                onSuccess = {
                    viewModelScope.launch{
                        if(it.result.resultCd == "0000") {
                            _responseDirectPayment.emit(ResponsePayAPI.DirectPaymentContent(it))
                        } else {
                            _responseDirectPayment.emit(ResponsePayAPI.ErrorMessage(it.result.advanceMsg))
                        }
                    }
                },
                onError = { viewModelScope.launch{ _responseDirectPayment.emit(ResponsePayAPI.ErrorMessage(it)) } },
                body = requestDirectPaymentDto
            )
        }
    }

    fun requestDirectCancelPayment(requestDirectCancelPaymentDto: RequestDirectCancelPaymentDto) {
        viewModelScope.launch {
            directPaymentRepository.refund(
                onSuccess = { viewModelScope.launch{ _responseDirectPayment.emit(ResponsePayAPI.DirectCancelPaymentContent(it)) } },
                onError = { viewModelScope.launch{ _responseDirectPayment.emit(ResponsePayAPI.ErrorMessage(it)) } },
                body = requestDirectCancelPaymentDto
            )
        }
    }
}