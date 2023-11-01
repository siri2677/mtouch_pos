package com.example.data.mapper.api


import com.example.data.entity.api.request.pay.RequestDirectCancelPaymentEntityRefund
import com.example.data.entity.api.request.pay.RequestDirectPaymentPay
import com.example.data.entity.api.request.tms.CheckTmsRequestBody
import com.example.data.entity.api.request.tms.KeyTmsRequestBody
import com.example.data.entity.api.request.tms.ListTmsRequestBody
import com.example.data.entity.api.request.tms.MchtNameTmsRequestBody
import com.example.data.entity.api.request.tms.RequestCancelPaymentBody
import com.example.data.entity.api.request.tms.ResponseInsertPaymentDataBody
import com.example.data.entity.api.request.tms.RequestPaymentBody
import com.example.data.entity.api.request.tms.StatisticsTmsRequestBody
import com.example.domain.dto.request.pay.RequestDirectCancelPaymentDto
import com.example.domain.dto.request.pay.RequestDirectPaymentDto
import com.example.domain.dto.request.tms.RequestCancelPaymentDTO
import com.example.domain.dto.request.tms.RequestDirectPaymentCheckDto
import com.example.domain.dto.request.tms.RequestGetPaymentListDto
import com.example.domain.dto.request.tms.RequestGetUserInformationDto
import com.example.domain.dto.request.tms.RequestInsertPaymentDataDTO
import com.example.domain.dto.request.tms.RequestPaymentDTO
import com.example.domain.dto.response.tms.ResponseGetMerchantNameDto
import com.example.domain.dto.response.tms.ResponseGetPaymentStatisticsDto
import org.mapstruct.Mapper

@Mapper
interface RequestDataMapper {
    fun keyToGetUserInformationModel(requestGetUserInformationDto: RequestGetUserInformationDto): KeyTmsRequestBody
    fun paymentDtoToEntity(requestPaymentDto: RequestPaymentDTO): RequestPaymentBody
    fun paymentCancelDtoToEntity(requestCancelPaymentDto: RequestCancelPaymentDTO): RequestCancelPaymentBody
    fun insertPaymentDataDtoToEntity(requestInsertPaymentDataDto: RequestInsertPaymentDataDTO): ResponseInsertPaymentDataBody
    fun mchtNameToGetMerchantNameModel(requestMerchantNameDto: ResponseGetMerchantNameDto): MchtNameTmsRequestBody
    fun listToGetPaymentListModel(requestGetPaymentListDto: RequestGetPaymentListDto): ListTmsRequestBody
    fun statisticsToGetPaymentStatisticsModel(requestGetPaymentStatisticsDto: ResponseGetPaymentStatisticsDto): StatisticsTmsRequestBody
    fun checkToDirectPaymentCheckModel(requestDirectPaymentCheckDto: RequestDirectPaymentCheckDto): CheckTmsRequestBody

    fun directPaymentDtoToEntity(requestDirectPaymentDto: RequestDirectPaymentDto): RequestDirectPaymentPay
    fun directCancelPaymentDtoToEntity(requestDirectCancelPaymentDto: RequestDirectCancelPaymentDto): RequestDirectCancelPaymentEntityRefund

}