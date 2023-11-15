package com.example.domain.repositoryInterface

import com.example.domain.dto.request.pay.RequestDirectCancelPaymentDto
import com.example.domain.dto.request.pay.RequestDirectPaymentDto
import com.example.domain.dto.response.pay.ResponseDirectCancelPaymentDto
import com.example.domain.dto.response.pay.ResponseDirectPaymentDto

interface DirectPaymentRepository {
    suspend fun approve(
        onSuccess: (ResponseDirectPaymentDto) -> Unit,
        onError: (String) -> Unit,
        body: RequestDirectPaymentDto
    )
    suspend fun refund(
        onSuccess: (ResponseDirectCancelPaymentDto) -> Unit,
        onError: (String) -> Unit,
        body: RequestDirectCancelPaymentDto
    )
}