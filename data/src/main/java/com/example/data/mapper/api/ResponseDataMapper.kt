package com.example.data.mapper.api

import com.example.data.entity.api.response.pay.ResponseDirectCancelPaymentEntity
import com.example.data.entity.api.response.pay.ResponseDirectPaymentEntity
import com.example.data.entity.api.response.tms.CheckTmsResponseBody
import com.example.data.entity.api.response.tms.KeyTmsResponseBody
import com.example.data.entity.api.response.tms.ListTmsResponseBody
import com.example.data.entity.api.response.tms.MchtNameTmsResponseBody
import com.example.data.entity.api.response.tms.ResponseCancelPaymentBody
import com.example.data.entity.api.response.tms.ResponseInsertPaymentDataBody
import com.example.data.entity.api.response.tms.ResponseCancelPaymentEntity
import com.example.data.entity.api.response.tms.ResponseInsertPaymentDataEntity
import com.example.data.entity.api.response.tms.ResponsePaymentBody
import com.example.data.entity.api.response.tms.ResponsePaymentEntity
import com.example.data.entity.api.response.tms.StatisticsTmsResponseBody
import com.example.data.entity.api.response.tms.SummaryTmsResponseBody
import com.example.domain.dto.response.pay.ResponseDirectCancelPaymentDto
import com.example.domain.dto.response.pay.ResponseDirectPaymentDto
import com.example.domain.dto.response.tms.ResponseCancelPaymentDTO
import com.example.domain.dto.response.tms.ResponseDirectPaymentCheckDto
import com.example.domain.dto.response.tms.ResponseGetMerchantNameDto
import com.example.domain.dto.response.tms.ResponseGetPaymentListDto
import com.example.domain.dto.response.tms.ResponseGetPaymentStatisticsDto
import com.example.domain.dto.response.tms.ResponseGetSummaryPaymentStatisticsDto
import com.example.domain.dto.response.tms.ResponseGetUserInformationDto
import com.example.domain.dto.response.tms.ResponseInsertPaymentDataDTO
import com.example.domain.dto.response.tms.ResponsePaymentDTO
import org.mapstruct.Mapper

@Mapper
interface ResponseDataMapper {
    fun keyToGetUserInformationModel(keyTmsResponseBody: KeyTmsResponseBody) : ResponseGetUserInformationDto
    fun paymentEntityToDto(responsePaymentBody: ResponsePaymentBody) : ResponsePaymentDTO
    fun cancelPaymentEntityToDto(responseCancelPaymentBody: ResponseCancelPaymentBody) : ResponseCancelPaymentDTO
    fun insertPaymentDataEntityToDto(responseInsertPaymentDataBody: ResponseInsertPaymentDataBody) : ResponseInsertPaymentDataDTO
    fun mchtNameToGetMerchantNameModel(mchtNameTmsResponseBody: MchtNameTmsResponseBody) : ResponseGetMerchantNameDto
    fun listToGetPaymentListModel(listTmsResponseBody: ListTmsResponseBody) : ResponseGetPaymentListDto
    fun statisticsToGetPaymentStatisticsModel(statisticsTmsResponseBody: StatisticsTmsResponseBody): ResponseGetPaymentStatisticsDto
    fun checkToDirectPaymentCheckModel(checkTmsResponseBody: CheckTmsResponseBody) : ResponseDirectPaymentCheckDto
    fun summaryToDirectPaymentCheckModel(summaryTmsResponseBody: SummaryTmsResponseBody) : ResponseGetSummaryPaymentStatisticsDto

    fun directPaymentEntityToDto(responseDirectPaymentEntity: ResponseDirectPaymentEntity) : ResponseDirectPaymentDto
    fun directCancelPaymentEntityToDto(responseDirectCancelPaymentEntity: ResponseDirectCancelPaymentEntity): ResponseDirectCancelPaymentDto
}