package com.kwonps.data.source.payment

import com.kwonps.data.common.dispatcher.DispatcherProvider
import com.kwonps.data.remote.apiservice.PayAPIService
import com.kwonps.data.remote.dto.request.RequestDirectPayment
import com.kwonps.data.remote.dto.response.ResponseDirectPayment
import javax.inject.Inject
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Direct-payment remote source. No local cache is used because VAN approvals must be
 * consistent with server-side reconciliation.
 */
class DirectPaymentRemoteDataSource @Inject constructor(
    private val apiService: PayAPIService,
    private val payKey: String,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun approve(request: RequestDirectPayment.DirectPayment): Response<ResponseDirectPayment.DirectPayment> =
        withContext(dispatcherProvider.io) { apiService.sendDirectPayment(payKey, request) }

    suspend fun cancel(request: RequestDirectPayment.DirectCancelPayment): Response<ResponseDirectPayment.DirectCancelPayment> =
        withContext(dispatcherProvider.io) { apiService.sendDirectRefund(payKey, request) }
}
