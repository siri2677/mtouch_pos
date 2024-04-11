package com.example.data.retrofit.repositoryimplement

import com.example.data.retrofit.ApiServiceImpl
import com.example.domain.model.request.pay.RequestPayModel
import com.example.domain.model.response.ResponseModel
import com.example.domain.model.response.tms.ResponseTmsModel
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnException
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.mapstruct.factory.Mappers
import java.nio.charset.StandardCharsets

class HandleRequestPay(
    private val payKey: String,
    private val deviceSharedFlow: MutableSharedFlow<ResponseModel>
) {
    private val apiService = ApiServiceImpl().getAPIDirectService()

    private suspend fun <T> ApiResponse<T>.errorHandling(): ApiResponse<T> {
        return this.suspendOnError {
            deviceSharedFlow.emit(
                ResponseTmsModel.Error(String(response.errorBody()!!.bytes(), StandardCharsets.UTF_8))
            )
        }.suspendOnException {
            deviceSharedFlow.emit(
                ResponseTmsModel.Error(message!!)
            )
        }
    }

    fun approve(
        requestPayDTO: RequestPayModel.DirectPaymentPay
    ) {
        CoroutineScope(Dispatchers.IO).launch() {
            apiService.sendDirectPayment(
                payKey = payKey,
                body = RequestPayModel.DirectPayment(requestPayDTO)
            ).suspendOnSuccess {
                deviceSharedFlow.emit(data)
            }.errorHandling()
        }
    }

    fun refund(
        requestPayDTO: RequestPayModel.DirectCancelPaymentRefund
    ) {
        CoroutineScope(Dispatchers.IO).launch() {
            apiService.sendDirectRefund(
                payKey = payKey,
                body = RequestPayModel.DirectCancelPayment(requestPayDTO)
            ).suspendOnSuccess {
                deviceSharedFlow.emit(data)
            }.errorHandling()
        }
    }
}