package com.example.data.retrofit.repositoryimplement

import android.content.Context
import com.example.data.sharedpreference.UserInformationSharedPreferenceImpl
import com.example.domain.model.request.RequestModel
import com.example.domain.model.request.tms.RequestTmsModel
import com.example.domain.model.response.ResponseModel
import com.example.domain.repositoryInterface.RequestRemoteRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class RequestRemoteTmsRepositoryImpl @Inject constructor(
    val context: Context
): RequestRemoteRepository {
    override fun invoke(responseModel: MutableStateFlow<ResponseModel>, requestModel: RequestModel) {
        val handleRequestTms = HandleRequestTms(
            token = UserInformationSharedPreferenceImpl(context).getUserInformation()?.key ?: "",
            deviceSharedFlow = responseModel
        )
        when(val requestTmsDTO = requestModel as RequestTmsModel) {
            is RequestTmsModel.CancelPayment -> handleRequestTms.refund(requestTmsDTO)
            is RequestTmsModel.DirectPaymentCheck -> TODO()
            is RequestTmsModel.GetMerchantName -> TODO()
            is RequestTmsModel.GetPaymentList -> handleRequestTms.list(requestTmsDTO)
            is RequestTmsModel.GetPaymentStatistics -> TODO()
            is RequestTmsModel.GetUserInformation -> handleRequestTms.key(context, requestTmsDTO)
            is RequestTmsModel.KsnetSocketCommunicate -> handleRequestTms.socketKsnet(requestTmsDTO)
            is RequestTmsModel.Payment -> handleRequestTms.approve(requestTmsDTO)
            is RequestTmsModel.GetSummaryPaymentStatistics -> handleRequestTms.summary()
        }
    }
}