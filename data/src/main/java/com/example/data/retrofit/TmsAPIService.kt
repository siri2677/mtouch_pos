package com.example.data.retrofit


import com.example.domain.model.request.tms.RequestTmsModel
import com.example.domain.model.response.tms.ResponseTmsModel

import com.skydoves.sandwich.ApiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface TmsAPIService {
    @Headers("Content-Type: application/json")
    @POST("/v0/key")
    suspend fun key(
        @Body body: DataFormat<RequestTmsModel.GetUserInformation>
    ): ApiResponse<DataFormat<ResponseTmsModel.GetUserInformation>>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/rule")
    suspend fun rule(
        @Header("Authorization") token: String?,
        @Body body: DataFormat<RequestTmsModel.Payment>
    ): ApiResponse<DataFormat<ResponseTmsModel.Payment>>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/crule")
    suspend fun crule(
        @Header("Authorization") token: String?,
        @Body body: DataFormat<RequestTmsModel.CancelPayment>
    ): ApiResponse<DataFormat<ResponseTmsModel.Payment>>

    @Headers("Content-Type: application/json")
    @POST("/v0/ksnet/socket")
    suspend fun socketKsnet(
        @Header("Authorization") token: String?,
        @Body body: RequestTmsModel.KsnetSocketCommunicate
    ): ApiResponse<DataFormat<ResponseTmsModel.KsnetSocketCommunicate>>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/check")
    fun check(
        @Header("Authorization") token: String?,
        @Body body: RequestTmsModel.DirectPaymentCheck
    ): ApiResponse<ResponseTmsModel.DirectPaymentCheck>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/summary")
    suspend fun summary(
        @Header("Authorization") token: String?
    ): ApiResponse<ResponseTmsModel.GetSummaryPaymentStatistics>

    @Headers("Content-Type: application/json")
    @POST("/v0/mcht/name")
    suspend fun mchtName(
        @Body body: RequestTmsModel.GetMerchantName
    ): ApiResponse<ResponseTmsModel.GetMerchantName>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/statistics")
    fun statistics(
        @Header("Authorization") token: String?,
        @Body body: DataFormat<RequestTmsModel.GetPaymentStatistics>
    ): ApiResponse<DataFormat<ResponseTmsModel.GetPaymentStatistics>>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/list")
    suspend fun list(
        @Header("Authorization") token: String?,
        @Body body: DataFormat<RequestTmsModel.GetPaymentList>
    ): ApiResponse<DataFormat<ResponseTmsModel.GetPaymentList>>
}