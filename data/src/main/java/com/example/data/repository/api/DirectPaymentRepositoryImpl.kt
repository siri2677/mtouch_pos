package com.example.data.repository.api

import com.example.data.entity.api.request.pay.RequestDirectCancelPaymentEntity
import com.example.data.entity.api.request.pay.RequestDirectPaymentEntity
import com.example.data.mapper.api.RequestDataMapper
import com.example.data.mapper.api.ResponseDataMapper
import com.example.data.retrofitInterface.ApiServiceRepositoryImpl
import com.example.domain.dto.request.pay.RequestDirectCancelPaymentDto
import com.example.domain.dto.request.pay.RequestDirectPaymentDto
import com.example.domain.dto.response.pay.ResponseDirectCancelPaymentDto
import com.example.domain.dto.response.pay.ResponseDirectPaymentDto
import com.example.domain.repositoryInterface.DirectPaymentRepository
import com.skydoves.sandwich.onError
import com.skydoves.sandwich.onException
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.mapstruct.factory.Mappers

class DirectPaymentRepositoryImpl: DirectPaymentRepository {
    override suspend fun approve(
        onSuccess: (ResponseDirectPaymentDto) -> Unit,
        onError: (String) -> Unit,
        body: RequestDirectPaymentDto
    ) = flow<Unit> {
        ApiServiceRepositoryImpl().getAPIDirectService().sendDirectPayment(
            body.payKey,
            RequestDirectPaymentEntity(Mappers.getMapper(RequestDataMapper::class.java).directPaymentDtoToEntity(body))
        ).suspendOnSuccess {
            onSuccess(Mappers.getMapper(ResponseDataMapper::class.java).directPaymentEntityToDto(data!!))
        }.onError() {
            onError(errorBody.toString())
        }.onException {
            onError(message!!)
        }
    }.flowOn(Dispatchers.IO).collect()

    override suspend fun refund(
        onSuccess: (ResponseDirectCancelPaymentDto) -> Unit,
        onError: (String) -> Unit,
        body: RequestDirectCancelPaymentDto
    ) = flow<Unit> {
        ApiServiceRepositoryImpl().getAPIDirectService().sendDirectRefund(
            body.payKey,
            RequestDirectCancelPaymentEntity(Mappers.getMapper(RequestDataMapper::class.java).directCancelPaymentDtoToEntity(body))
        ).suspendOnSuccess {
            onSuccess(Mappers.getMapper(ResponseDataMapper::class.java).directCancelPaymentEntityToDto(data!!))
        }.onError() {
            onError(errorBody.toString())
        }.onException {
            onError(message!!)
        }
    }.flowOn(Dispatchers.IO).collect()


}