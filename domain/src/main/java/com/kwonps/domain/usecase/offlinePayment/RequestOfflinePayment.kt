package com.kwonps.domain.usecase.offlinePayment

import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.payment.OfflinePaymentData
import com.kwonps.domain.model.payment.VanData
import com.kwonps.domain.repository.OfflinePaymentRepository
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

