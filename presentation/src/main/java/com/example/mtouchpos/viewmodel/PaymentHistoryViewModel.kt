package com.example.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.response.ResponseModel
import com.example.domain.model.response.tms.ResponseTmsModel
import com.example.domain.repositoryInterface.RequestRemoteRepository
import com.example.mtouchpos.dto.PaymentPeriod
import com.example.mtouchpos.dto.ResponseFlowData
import com.example.mtouchpos.hilt.TmsRepository
import com.example.mtouchpos.mapper.RequestDataMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.mapstruct.factory.Mappers
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    @TmsRepository private val RequestRemoteTmsRepositoryImpl: RequestRemoteRepository
) : ViewModel(), Serializable {
    private val responseModel = MutableStateFlow<ResponseModel>(ResponseTmsModel.Init)
    val responseFlowData = MutableStateFlow<ResponseFlowData>(ResponseFlowData.Init)
    var paymentPeriod = PaymentPeriod("","")

    fun getPaymentList(paymentPeriod: PaymentPeriod) {
        this.paymentPeriod = paymentPeriod
        RequestRemoteTmsRepositoryImpl(
            requestModel = Mappers.getMapper(RequestDataMapper::class.java)
                .toRequestGetPaymentListEntity(paymentPeriod),
            responseModel = responseModel
        )
        handleResponseModel()
    }

    private fun handleResponseModel() {
        if(responseModel.value == ResponseTmsModel.Init) {
            viewModelScope.launch {
                responseModel.collect{
                    when(it) {
                        is ResponseTmsModel.GetPaymentList -> {
                            val creditPaymentList = ArrayList<ResponseFlowData.CreditPayment>()
                            it.list.map { listContents ->
                                creditPaymentList.add(
                                    Mappers.getMapper(RequestDataMapper::class.java)
                                        .toCreditPayment(listContents)
                                )
                            }
                            responseFlowData.emit(ResponseFlowData.CreditPaymentList(creditPaymentList))
                        }
                        is ResponseTmsModel.Error -> {
                            responseFlowData.emit(ResponseFlowData.Error(it.message))
                        }
                    }
                }
            }
        }
    }
}