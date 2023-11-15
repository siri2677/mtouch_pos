package com.example.data.repository.api

import android.content.Context
import com.example.data.dataBase.AppDatabase
import com.example.data.mapper.api.RequestDataMapper
import com.example.data.mapper.api.ResponseDataMapper
import com.example.data.retrofitInterface.ApiServiceRepositoryImpl
import com.example.data.sharedpreference.UserInformationSharedPreferenceImpl
import com.example.domain.repositoryInterface.LoginRepository
import com.example.data.entity.api.request.tms.KeyTmsRequestData
import com.example.data.repository.db.UserInformationRepositoryImpl
import com.example.domain.dto.request.tms.RequestGetMerchantNameDto
import com.example.domain.dto.request.tms.RequestGetUserInformationDto
import com.example.domain.dto.response.tms.ResponseGetSummaryPaymentStatisticsDto
import com.example.domain.dto.response.tms.ResponseGetUserInformationDto
import com.skydoves.sandwich.onError
import com.skydoves.sandwich.onException
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnException
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.mapstruct.factory.Mappers
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val context: Context
): LoginRepository {
    override fun key(
        onSuccess: (ResponseGetUserInformationDto) -> Unit,
        summary: () -> Unit,
        onError: (String) -> Unit,
        body: RequestGetUserInformationDto
    ) {
        CoroutineScope(Dispatchers.IO).launch() {
            flow<Unit> {
                ApiServiceRepositoryImpl().getAPIService().key(
                    KeyTmsRequestData(Mappers.getMapper(RequestDataMapper::class.java).keyToGetUserInformationModel(body))
                ).suspendOnSuccess {
                    val responseData = Mappers.getMapper(ResponseDataMapper::class.java).keyToGetUserInformationModel(data.data!!)
                    UserInformationSharedPreferenceImpl(context).setUserInformation(responseData)
                    UserInformationRepositoryImpl(AppDatabase.getInstance(context).userInformationDao()).insertUserInformation(body)
                    onSuccess(responseData)
                    summary()
                }.onError() {
                    onError(errorBody.toString())
                }.onException {
                    onError(message!!)
                }
            }.flowOn(Dispatchers.IO).collect()
        }
    }

    override fun summary(
        onSuccess: (ResponseGetSummaryPaymentStatisticsDto) -> Unit,
        onError: (String) -> Unit,
        token: String
    ) {
        CoroutineScope(Dispatchers.IO).launch() {
            flow<Unit> {
                ApiServiceRepositoryImpl().getAPIService().summary(token).suspendOnSuccess {
                    onSuccess(
                        Mappers.getMapper(ResponseDataMapper::class.java)
                            .summaryToDirectPaymentCheckModel(data.data!!)
                    )
                }.suspendOnError {
                    onError(errorBody.toString())
                }.suspendOnException {
                    onError(message!!)
                }
            }.flowOn(Dispatchers.IO)
        }
    }

    override fun mchtName(
        onComplete: () -> Unit,
        onError: (String?) -> Unit,
        body: RequestGetMerchantNameDto
    ) {

    }



}