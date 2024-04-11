package com.example.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.response.ResponseModel
import com.example.domain.model.response.pay.ResponsePayModel
import com.example.domain.repositoryInterface.RequestRemoteRepository
import com.example.mtouchpos.dto.AmountInfo
import com.example.mtouchpos.dto.DirectPaymentInfo
import com.example.mtouchpos.dto.ResponseFlowData
import com.example.mtouchpos.dto.RootPaymentInfo
import com.example.mtouchpos.hilt.PayRepository
import com.example.mtouchpos.mapper.RequestDataMapper
import com.example.mtouchpos.vo.PaymentType
import com.example.mtouchpos.vo.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.mapstruct.factory.Mappers
import javax.inject.Inject

@HiltViewModel
class DirectPaymentViewModel @Inject constructor(
    @PayRepository private val RequestRemotePayRepositoryImpl: RequestRemoteRepository
): ViewModel(){
    private val responseModel = MutableStateFlow<ResponseModel>(ResponsePayModel.Init)
    val responseFlowData = MutableStateFlow<ResponseFlowData>(ResponseFlowData.Init)

    fun requestDirectPayment(
        amountInfo: AmountInfo,
        directPaymentInfo: DirectPaymentInfo,
    ) {
        RequestRemotePayRepositoryImpl(
            responseModel = responseModel,
            requestModel = Mappers.getMapper(RequestDataMapper::class.java)
                .toRequestDirectPaymentPay(
                    amountInfo = amountInfo,
                    directPaymentInfo = directPaymentInfo
                )
        )
        handleResponseModel(null, amountInfo.installment)
    }

    fun requestDirectCancelPayment(
        cardNumber: String,
        amountInfo: AmountInfo,
        rootPaymentInfo: RootPaymentInfo
    ) {
        RequestRemotePayRepositoryImpl(
            responseModel = responseModel,
            requestModel = Mappers.getMapper(RequestDataMapper::class.java)
                .toRequestDirectCancelPaymentRefund(
                    amountInfo = amountInfo,
                    rootPaymentInfo = rootPaymentInfo
                )
        )
        handleResponseModel(cardNumber, amountInfo.installment)
    }

    private fun handleResponseModel(
        requestCardNumber: String?,
        requestInstallment: String
    ) {
        if(responseModel.value == ResponsePayModel.Init){
            viewModelScope.launch {
                responseModel.collect{
                    when(it) {
                        is ResponsePayModel.DirectPayment -> {
                            if(it.result.resultCd == "0000") {
                                responseFlowData.emit(
                                    Mappers.getMapper(RequestDataMapper::class.java)
                                        .toCompleteDirectPayment(
                                            transactionType = TransactionType.Direct,
                                            paymentType = PaymentType.Approve,
                                            directPayment = it
                                        )
                                )
                            } else {
                                responseFlowData.emit(ResponseFlowData.Error(it.result.advanceMsg))
                            }
                        }
                        is ResponsePayModel.DirectCancelPayment -> {
                            if(it.result.resultCd == "0000") {
                                responseFlowData.emit(
                                    Mappers.getMapper(RequestDataMapper::class.java)
                                        .toCompleteDirectCancelPayment(
                                            cardNumber = requestCardNumber!!,
                                            installment = requestInstallment,
                                            transactionType = TransactionType.Direct,
                                            paymentType = PaymentType.Refund,
                                            directCancelPayment = it
                                        )
                                )
                            } else {
                                responseFlowData.emit(ResponseFlowData.Error(it.result.advanceMsg))
                            }
                        }
                    }
                }
            }
        }
    }
}