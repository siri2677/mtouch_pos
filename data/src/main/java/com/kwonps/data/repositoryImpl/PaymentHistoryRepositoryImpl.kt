package com.kwonps.data.repositoryImpl

import com.kwonps.data.common.interceptor.FlowCallDecorator
import com.kwonps.data.mapper.PaymentHistoryMapper
import com.kwonps.data.remote.handleApiResult
import com.kwonps.data.source.payment.PaymentHistoryRemoteDataSource
import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.paymentHistory.DailyAndMonthlyPaymentStatisticData
import com.kwonps.domain.model.paymentHistory.PaymentHistoryData
import com.kwonps.domain.model.paymentHistory.PaymentStatisticData
import com.kwonps.domain.model.paymentHistory.PeriodData
import com.kwonps.domain.repository.PaymentHistoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Remote-first payment history repository. Cache strategy: stateless remote reads only;
 * synchronization relies on the upstream VAN/TMS server. Common logging and retry
 * behavior is centralized in [flowCallDecorator].
 */
class PaymentHistoryRepositoryImpl @Inject constructor(
    private val remoteDataSource: PaymentHistoryRemoteDataSource,
    private val mapper: PaymentHistoryMapper,
    private val flowCallDecorator: FlowCallDecorator
) : PaymentHistoryRepository {
    override suspend fun searchPaymentList(periodInfo: PeriodData): Flow<ApiResult<List<PaymentHistoryData>>> =
        flowCallDecorator.asResultFlow("paymentHistory:searchPaymentList") {
            remoteDataSource.fetchPaymentList(periodInfo)
                .handleApiResult { mapper.toDomainList(it.data.list) }
        }

    override suspend fun searchPaymentStatistic(periodInfo: PeriodData): Flow<ApiResult<PaymentStatisticData>> =
        flowCallDecorator.asResultFlow("paymentHistory:searchPaymentStatistic") {
            remoteDataSource.fetchPaymentStatistics(periodInfo)
                .handleApiResult { mapper.toStatistic(it.data) }
        }

    override suspend fun searchPaymentStatistic(): Flow<ApiResult<DailyAndMonthlyPaymentStatisticData>> =
        flowCallDecorator.asResultFlow("paymentHistory:searchPaymentStatisticSummary") {
            remoteDataSource.fetchSummaryPaymentStatistics()
                .handleApiResult { mapper.toDailyAndMonthlyStatistic(it.data) }
        }

    override suspend fun checkDirectPayment(trxId: String): Flow<ApiResult<Boolean>> =
        flowCallDecorator.asResultFlow("paymentHistory:checkDirectPayment") {
            remoteDataSource.checkDirectPayment(trxId)
                .handleApiResult { it.data.trxType == "ONTR" }
        }
}
