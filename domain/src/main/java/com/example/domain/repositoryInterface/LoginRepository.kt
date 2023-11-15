package com.example.domain.repositoryInterface

import com.example.domain.dto.request.tms.RequestGetMerchantNameDto
import com.example.domain.dto.request.tms.RequestGetUserInformationDto
import com.example.domain.dto.response.tms.ResponseGetSummaryPaymentStatisticsDto
import com.example.domain.dto.response.tms.ResponseGetUserInformationDto

interface LoginRepository {
    fun key(
        onSuccess: (ResponseGetUserInformationDto) -> Unit,
        summary: () -> Unit,
        onError: (String) -> Unit,
        body: RequestGetUserInformationDto
    )
    fun summary(
        onSuccess: (ResponseGetSummaryPaymentStatisticsDto) -> Unit,
        onError: (String) -> Unit,
        token: String
    )
    fun mchtName(
        onComplete: () -> Unit,
        onError: (String?) -> Unit,
        body: RequestGetMerchantNameDto
    )
}