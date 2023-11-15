package com.example.domain.repositoryInterface

import com.example.domain.dto.request.tms.RequestGetMerchantNameDto
import com.example.domain.dto.request.tms.RequestGetUserInformationDto
import com.example.domain.dto.response.tms.ResponseGetMerchantNameDto
import com.example.domain.dto.response.tms.ResponseGetSummaryPaymentStatisticsDto
import com.example.domain.dto.response.tms.ResponseGetUserInformationDto

interface LoginRelatedRepository {
    fun key(
        onSuccess: (ResponseGetUserInformationDto) -> Unit,
        summary: suspend () -> Unit,
        onError: (String) -> Unit,
        body: RequestGetUserInformationDto
    )
    suspend fun summary(
        onSuccess: (ResponseGetSummaryPaymentStatisticsDto) -> Unit,
        onError: (String) -> Unit,
        token: String
    )
    suspend fun mchtName(
        onComplete: () -> Unit,
        onError: (String?) -> Unit,
        body: RequestGetMerchantNameDto
    )
}