package com.example.data.apiService

import com.example.data.entity.api.request.tms.CheckTmsRequestData
import com.example.data.entity.api.request.tms.KeyTmsRequestData
import com.example.data.entity.api.request.tms.ListTmsRequestData
import com.example.data.entity.api.request.tms.MchtNameTmsRequestData
import com.example.data.entity.api.request.tms.RequestInsertPaymentDataEntity
import com.example.data.entity.api.request.tms.RequestCancelPaymentEntity
import com.example.data.entity.api.request.tms.RequestPaymentEntity
import com.example.data.entity.api.request.tms.StatisticsTmsRequestData
import com.example.data.entity.api.response.tms.ResponseCancelPaymentEntity
import com.example.data.entity.api.response.tms.CheckTmsResponseData
import com.example.data.entity.api.response.tms.KeyTmsResponseData
import com.example.data.entity.api.response.tms.ListTmsResponseData
import com.example.data.entity.api.response.tms.MchtNameTmsResponseData
import com.example.data.entity.api.response.tms.ResponseInsertPaymentDataEntity
import com.example.data.entity.api.response.tms.ResponsePaymentEntity
import com.example.data.entity.api.response.tms.StatisticsTmsResponseData
import com.example.data.entity.api.response.tms.SummaryTmsResponseData
import com.skydoves.sandwich.ApiResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface SvcTmsAPIService {
    @Headers("Content-Type: application/json")
    @GET("/v0/key")
    fun key(
        @Header("Authorization") token: String?
    ): ApiResponse<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/v0/key")
    suspend fun key(
        @Body body: KeyTmsRequestData
    ): ApiResponse<KeyTmsResponseData>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/rule")
    suspend fun rule(
        @Header("Authorization") token: String?,
        @Body body: RequestPaymentEntity
    ): ApiResponse<ResponsePaymentEntity>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/crule")
    fun crule(
        @Header("Authorization") token: String?,
        @Body body: RequestCancelPaymentEntity
    ): ApiResponse<ResponseCancelPaymentEntity>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/push")
    suspend fun push(
        @Header("Authorization") token: String?,
        @Body body: RequestInsertPaymentDataEntity
    ): ApiResponse<ResponseInsertPaymentDataEntity>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/check")
    fun check(
        @Header("Authorization") token: String?,
        @Body body: CheckTmsRequestData
    ): ApiResponse<CheckTmsResponseData>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/summary")
    suspend fun summary(
        @Header("Authorization") token: String?
    ): ApiResponse<SummaryTmsResponseData>

    @Headers("Content-Type: application/json")
    @POST("/v0/mcht/name")
    suspend fun mchtName(
        @Body body: MchtNameTmsRequestData
    ): ApiResponse<MchtNameTmsResponseData>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/statistics")
    fun statistics(
        @Header("Authorization") token: String?,
        @Body body: StatisticsTmsRequestData
    ): ApiResponse<StatisticsTmsResponseData>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/list")
    suspend fun list(
        @Header("Authorization") token: String?,
        @Body body: ListTmsRequestData
    ): ApiResponse<ListTmsResponseData>
}