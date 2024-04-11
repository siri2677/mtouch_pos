package com.example.data.retrofit.repositoryimplement

import android.content.Context
import com.example.data.sharedpreference.UserInformationSharedPreferenceImpl
import com.example.domain.model.request.RequestModel
import com.example.domain.model.request.pay.RequestPayModel
import com.example.domain.model.request.tms.RequestTmsModel
import com.example.domain.model.response.ResponseModel
import com.example.domain.model.response.tms.ResponseTmsModel
import com.example.domain.repositoryInterface.RequestRemoteRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class RequestRemotePayRepositoryImpl @Inject constructor(
    val context: Context
): RequestRemoteRepository {
    override fun invoke(responseModel: MutableStateFlow<ResponseModel>, requestModel: RequestModel) {
        val handleRequestPay = HandleRequestPay(
            payKey = UserInformationSharedPreferenceImpl(context).getUserInformation()?.payKey ?: "",
            deviceSharedFlow = responseModel
        )
        when(val requestPayDTO = requestModel as RequestPayModel) {
            is RequestPayModel.DirectPaymentPay -> handleRequestPay.approve(requestPayDTO)
            is RequestPayModel.DirectCancelPaymentRefund -> handleRequestPay.refund(requestPayDTO)
        }
    }
}