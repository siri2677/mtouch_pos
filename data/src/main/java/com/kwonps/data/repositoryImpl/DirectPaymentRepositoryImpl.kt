package com.kwonps.data.repositoryImpl

import com.kwonps.data.common.interceptor.FlowCallDecorator
import com.kwonps.data.mapper.DirectPaymentMapper
import com.kwonps.data.remote.handleApiResultDetail
import com.kwonps.data.source.payment.DirectPaymentRemoteDataSource
import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.payment.DirectPaymentData
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.repository.DirectPaymentRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Direct payment repository backed by the VAN endpoint. Cache strategy: none; each
 * transaction is processed remotely and reflected immediately. Synchronization uses the
 * FlowCallDecorator to enforce retry and logging.
 */
class DirectPaymentRepositoryImpl @Inject constructor(
    private val remoteDataSource: DirectPaymentRemoteDataSource,
    private val mapper: DirectPaymentMapper,
    private val flowCallDecorator: FlowCallDecorator
) : DirectPaymentRepository {
    override suspend fun approve(
        directPaymentInfo: DirectPaymentData.Approve
    ): Flow<ApiResult<PaymentDetailData>> =
        flowCallDecorator.asResultFlow("directPayment:approve") {
            remoteDataSource.approve(mapper.toApproveRequest(directPaymentInfo))
                .handleApiResultDetail { response ->
                    if (response.result.resultCd == "0000") {
                        ApiResult.Success(mapper.toPaymentDetail(response))
                    } else {
                        ApiResult.Error(response.result.advanceMsg)
                    }
                }
        }

    override suspend fun cancel(
        directPaymentInfo: DirectPaymentData.Cancel
    ): Flow<ApiResult<PaymentDetailData>> =
        flowCallDecorator.asResultFlow("directPayment:cancel") {
            remoteDataSource.cancel(mapper.toCancelRequest(directPaymentInfo))
                .handleApiResultDetail { response ->
                    if (response.result.resultCd == "0000") {
                        ApiResult.Success(mapper.toCancelledPaymentDetail(response, directPaymentInfo))
                    } else {
                        ApiResult.Error(response.result.advanceMsg)
                    }
                }
        }
}
