package com.example.data.repository.api

import android.util.Log
import com.example.data.entity.api.request.pay.RequestDirectPaymentEntity
import com.example.data.entity.api.request.tms.ListTmsRequestData
import com.example.data.entity.api.response.tms.ListTmsResponseBody
import com.example.data.entity.api.response.tms.ListTmsResponseData
import com.example.data.mapper.api.RequestDataMapper
import com.example.data.mapper.api.ResponseDataMapper
import com.example.data.retrofitInterface.ApiServiceRepositoryImpl
import com.example.domain.dto.request.tms.RequestGetPaymentListDto
import com.example.domain.dto.request.tms.RequestGetPaymentStatisticsDto
import com.example.domain.dto.response.tms.ResponseGetPaymentListDto
import com.example.domain.dto.response.tms.ResponseGetPaymentStatisticsDto
import com.example.domain.repositoryInterface.GetPaymentInformationRepository
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnException
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.mapstruct.factory.Mappers

class GetPaymentInformationRepositoryImpl: GetPaymentInformationRepository {
    override suspend fun statistics(
        onSuccess: (ResponseGetPaymentStatisticsDto) -> Unit,
        onError: (String) -> Unit,
        token: String?,
        body: RequestGetPaymentStatisticsDto
    ) = flow<Unit> {
        TODO("Not yet implemented")
    }

    override fun list(
        onSuccess: (ResponseGetPaymentListDto) -> Unit,
        onError: (String) -> Unit,
        token: String?,
        body: RequestGetPaymentListDto
    ) {
        CoroutineScope(Dispatchers.IO).launch() {
            flow<Unit> {
                ApiServiceRepositoryImpl().getAPIService().list(
                    token,
                    ListTmsRequestData(Mappers.getMapper(RequestDataMapper::class.java).listToGetPaymentListModel(body))
                ).suspendOnSuccess {
                    Log.w("suspendOnSuccess", "suspendOnSuccess")
                    onSuccess(Mappers.getMapper(ResponseDataMapper::class.java).listToGetPaymentListModel(data.data!!))
                }.suspendOnError {
                    onError(errorBody.toString())
                }.suspendOnException {
                    onError(message!!)
                }
            }.flowOn(Dispatchers.IO).collect()
        }
    }
}

