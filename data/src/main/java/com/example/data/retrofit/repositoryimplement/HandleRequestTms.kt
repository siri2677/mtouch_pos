package com.example.data.retrofit.repositoryimplement

import android.content.Context
import com.example.data.room.UserInformationDatabase
import com.example.data.retrofit.DataFormat
import com.example.data.room.repositoryimplement.UserInformationRepositoryImpl
import com.example.data.retrofit.ApiServiceImpl
import com.example.data.sharedpreference.UserInformationSharedPreferenceImpl
import com.example.domain.model.request.tms.RequestTmsModel
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

class HandleRequestTms(
    private val token: String?,
    private val deviceSharedFlow: MutableSharedFlow<ResponseModel>
) {
    private val apiService = ApiServiceImpl().getAPIService()

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

    fun key(
        context: Context,
        requestTmsDTO: RequestTmsModel.GetUserInformation
    ) {
        CoroutineScope(Dispatchers.IO).launch() {
            apiService.key(
                DataFormat(requestTmsDTO)
            ).suspendOnSuccess {
                val responseData = data.data
                UserInformationSharedPreferenceImpl(context).setUserInformation(responseData)
                UserInformationRepositoryImpl(UserInformationDatabase.getInstance(context).userInformationDao()).insertUserInformation(requestTmsDTO)
                deviceSharedFlow.emit(responseData)
            }.errorHandling()
        }
    }

    fun summary() {
        CoroutineScope(Dispatchers.IO).launch() {
            apiService.summary(
                token
            ).suspendOnSuccess {
                deviceSharedFlow.emit(data)
            }.errorHandling()
        }
    }

//    fun mchtName(
//        onComplete: () -> Unit,
//        onError: (String?) -> Unit,
//        body: RequestGetMerchantNameDto
//    ) {
//
//    }
//
//    fun statistics(
//        onSuccess: (ResponseGetPaymentStatisticsDto) -> Unit,
//        onError: (String) -> Unit,
//        token: String?,
//        body: RequestGetPaymentStatisticsDto
//    ) {
//
//    }

    fun list(requestPaymentDTO: RequestTmsModel.GetPaymentList) {
        CoroutineScope(Dispatchers.IO).launch() {
            apiService.list(
                token,
                DataFormat(requestPaymentDTO)
            ).suspendOnSuccess {
                deviceSharedFlow.emit(data.data)
            }.errorHandling()
        }
    }


    fun approve(requestPaymentDTO: RequestTmsModel.Payment) {
        CoroutineScope(Dispatchers.IO).launch {
            apiService.rule(
                token,
                DataFormat(requestPaymentDTO)
            ).suspendOnSuccess {
                deviceSharedFlow.emit(data.data)
            }.errorHandling()
        }
    }

    fun refund(requestTmsDTO: RequestTmsModel.CancelPayment) {
        CoroutineScope(Dispatchers.IO).launch() {
            apiService.crule(
                token,
                DataFormat(requestTmsDTO)
            ).suspendOnSuccess {
                deviceSharedFlow.emit(data.data)
            }.errorHandling()
        }
    }

    fun socketKsnet(requestTmsDTO: RequestTmsModel.KsnetSocketCommunicate) {
        CoroutineScope(Dispatchers.IO).launch() {
            apiService.socketKsnet(
                token,
                requestTmsDTO
            ).suspendOnSuccess {
                deviceSharedFlow.emit(data.data)
            }.errorHandling()
        }
    }
}