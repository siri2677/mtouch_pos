package com.kwonps.domain.repository

import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.payment.DirectPaymentData
import com.kwonps.domain.model.payment.PaymentDetailData
import kotlinx.coroutines.flow.Flow

interface DirectPaymentRepository {
    suspend fun approve(
        directPaymentInfo: DirectPaymentData.Approve
    ): Flow<ApiResult<PaymentDetailData>>

    suspend fun cancel(
        directPaymentInfo: DirectPaymentData.Cancel
    ): Flow<ApiResult<PaymentDetailData>>
}