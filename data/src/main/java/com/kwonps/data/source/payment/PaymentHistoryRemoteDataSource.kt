package com.kwonps.data.source.payment

import com.kwonps.data.common.dispatcher.DispatcherProvider
import com.kwonps.data.mapper.PaymentHistoryMapper
import com.kwonps.data.remote.DataFormat
import com.kwonps.data.remote.apiservice.TmsAPIService
import com.kwonps.data.remote.dto.response.ResponsePaymentHistory
import com.kwonps.domain.model.paymentHistory.PeriodData
import javax.inject.Inject
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Remote-only source for payment history. Cache strategy: none, because history and
 * statistics must always reflect server state. Synchronization policy: each network call
 * is executed on the IO dispatcher and retried via FlowCallDecorator.
 */
class PaymentHistoryRemoteDataSource @Inject constructor(
    private val apiService: TmsAPIService,
    private val token: String,
    private val dispatcherProvider: DispatcherProvider,
    private val mapper: PaymentHistoryMapper,
) {
    suspend fun fetchPaymentList(periodInfo: PeriodData): Response<ResponsePaymentHistory.GetPaymentList> =
        withContext(dispatcherProvider.io) {
            apiService.list(
                token = token,
                body = DataFormat(mapper.toListRequest(periodInfo))
            )
        }

    suspend fun fetchPaymentStatistics(periodInfo: PeriodData): Response<ResponsePaymentHistory.GetPaymentStatistics> =
        withContext(dispatcherProvider.io) {
            apiService.statistics(
                token = token,
                body = DataFormat(mapper.toStatisticRequest(periodInfo))
            )
        }

    suspend fun fetchSummaryPaymentStatistics(): Response<ResponsePaymentHistory.GetSummaryPaymentStatistics> =
        withContext(dispatcherProvider.io) { apiService.summary(token) }

    suspend fun checkDirectPayment(trxId: String): Response<ResponsePaymentHistory.DirectPaymentCheck> =
        withContext(dispatcherProvider.io) {
            apiService.check(token, DataFormat(mapper.toDirectPaymentCheck(trxId)))
        }
}
