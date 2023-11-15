package com.example.domain.repositoryInterface

import com.example.domain.dto.request.tms.RequestGetPaymentListDto
import com.example.domain.dto.request.tms.RequestGetPaymentStatisticsDto
import com.example.domain.dto.response.pay.ResponseDirectPaymentDto
import com.example.domain.dto.response.tms.ResponseGetPaymentListDto
import com.example.domain.dto.response.tms.ResponseGetPaymentStatisticsDto

interface GetPaymentInformationRepository {
    suspend fun statistics(
        onSuccess: (ResponseGetPaymentStatisticsDto) -> Unit,
        onError: (String) -> Unit,
        token: String?,
        body: RequestGetPaymentStatisticsDto
    )
    fun list(
        onSuccess: (ResponseGetPaymentListDto) -> Unit,
        onError: (String) -> Unit,
        token: String?,
        body: RequestGetPaymentListDto
    )
}