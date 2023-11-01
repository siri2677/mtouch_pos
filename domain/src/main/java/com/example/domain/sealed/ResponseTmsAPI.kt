package com.example.domain.sealed

import com.example.domain.dto.response.tms.ResponseCancelPaymentDTO
import com.example.domain.dto.response.tms.ResponseDirectPaymentCheckDto
import com.example.domain.dto.response.tms.ResponseGetMerchantNameDto
import com.example.domain.dto.response.tms.ResponseGetPaymentListDto
import com.example.domain.dto.response.tms.ResponseGetPaymentStatisticsDto
import com.example.domain.dto.response.tms.ResponseGetSummaryPaymentStatisticsDto
import com.example.domain.dto.response.tms.ResponseGetUserInformationDto
import com.example.domain.dto.response.tms.ResponseInsertPaymentDataDTO
import com.example.domain.dto.response.tms.ResponsePaymentDTO

sealed class ResponseTmsAPI {
    data class CancelPayment(val responseCancelPaymentDto: ResponseCancelPaymentDTO): ResponseTmsAPI()
    data class DirectPaymentCheck(val responseDirectPaymentCheckDto: ResponseDirectPaymentCheckDto): ResponseTmsAPI()
    data class GetMerchantName(val responseGetMerchantNameDto: ResponseGetMerchantNameDto): ResponseTmsAPI()
    data class GetPaymentList(val responseGetPaymentListDto: ResponseGetPaymentListDto): ResponseTmsAPI()
    data class GetPaymentStatistics(val responseGetPaymentStatisticsDto: ResponseGetPaymentStatisticsDto): ResponseTmsAPI()
    data class GetSummaryPaymentStatistics(val responseGetSummaryPaymentStatisticsDto: ResponseGetSummaryPaymentStatisticsDto): ResponseTmsAPI()
    data class GetUserInformation(val responseGetUserInformationDto: ResponseGetUserInformationDto): ResponseTmsAPI()
    data class InsertPaymentData(val responseInsertPaymentDataDto: ResponseInsertPaymentDataDTO): ResponseTmsAPI()
    data class Payment(val responsePaymentDto: ResponsePaymentDTO): ResponseTmsAPI()
    data class ErrorMessage(val message: String): ResponseTmsAPI()
}