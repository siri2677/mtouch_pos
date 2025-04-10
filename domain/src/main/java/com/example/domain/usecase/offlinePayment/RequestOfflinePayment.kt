package com.example.domain.usecase.offlinePayment

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.VanData
import com.example.domain.repository.OfflinePaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RequestOfflinePayment @Inject constructor(
    private val offlinePaymentRepository: OfflinePaymentRepository
) {
    operator fun invoke(
        offlinePaymentData: OfflinePaymentData
    ): Flow<ApiResult<VanData>> = flow {
        offlinePaymentRepository(offlinePaymentData).collect{ emit(it) }
    }
}

