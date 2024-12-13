package com.example.domain.repository

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.DirectPaymentData
import com.example.domain.model.payment.PaymentDetailData
import kotlinx.coroutines.flow.Flow

interface DirectPaymentRepository {
    suspend fun approve(
        directPaymentInfo: DirectPaymentData.Approve
    ): Flow<ApiResult<PaymentDetailData>>

    suspend fun cancel(
        directPaymentInfo: DirectPaymentData.Cancel
    ): Flow<ApiResult<PaymentDetailData>>
}