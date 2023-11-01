package com.example.domain.repositoryInterface

import com.example.domain.dto.request.tms.RequestCancelPaymentDTO
import com.example.domain.dto.request.tms.RequestInsertPaymentDataDTO
import com.example.domain.dto.request.tms.RequestPaymentDTO
import com.example.domain.dto.response.tms.ResponseCancelPaymentDTO
import com.example.domain.dto.response.tms.ResponseInsertPaymentDataDTO
import com.example.domain.dto.response.tms.ResponsePaymentDTO

interface OfflinePaymentRepository {
    fun approve(
        onSuccess: (ResponsePaymentDTO) -> Unit,
        onError: (String) -> Unit,
        body: RequestPaymentDTO
    )
    fun refund(
        onSuccess: (ResponseCancelPaymentDTO) -> Unit,
        onError: (String) -> Unit,
        body: RequestCancelPaymentDTO
    )
    fun push(
        onSuccess: (ResponseInsertPaymentDataDTO) -> Unit,
        onError: (String) -> Unit,
        body: RequestInsertPaymentDataDTO,
        requestTelegram: ByteArray
    )
}