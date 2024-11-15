package com.example.data.remote.apiservice


import com.example.data.dto.request.RequestOffPayment
import com.example.data.dto.request.RequestPaymentHistory
import com.example.data.dto.request.RequestUser
import com.example.data.dto.response.ResponseOffPayment
import com.example.data.dto.response.ResponsePaymentHistory
import com.example.data.dto.response.ResponseUser
import com.example.data.remote.DataFormat
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface TmsAPIService {
    @Headers("Content-Type: application/json")
    @POST("/v0/key")
    suspend fun key(
        @Body body: DataFormat<RequestUser.Login>
    ): Response<DataFormat<ResponseUser.Login>>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/rule")
    suspend fun rule(
        @Header("Authorization") token: String?,
        @Body body: DataFormat<RequestOffPayment.Payment>
    ): Response<DataFormat<ResponseOffPayment.Payment>>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/crule")
    suspend fun crule(
        @Header("Authorization") token: String?,
        @Body body: DataFormat<RequestOffPayment.CancelPayment>
    ): Response<DataFormat<ResponseOffPayment.Payment>>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/pay/push")
    suspend fun push(
        @Header("Authorization") token: String?,
        @Body body: DataFormat<RequestOffPayment.Push>
    ): Response<DataFormat<ResponseOffPayment.Push>>

    @Headers("Content-Type: application/json")
    @POST("/v0/ksnet/socket")
    suspend fun socketKsnet(
        @Header("Authorization") token: String?,
        @Body body: RequestOffPayment.KsnetSocketCommunicate
    ): Response<DataFormat<ResponseOffPayment.KsnetSocketCommunicate>>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/check")
    fun check(
        @Header("Authorization") token: String,
        @Body body: DataFormat<RequestPaymentHistory.DirectPaymentCheck>
    ): Response<DataFormat<ResponsePaymentHistory.DirectPaymentCheck>>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/summary")
    suspend fun summary(
        @Header("Authorization") token: String
    ): Response<DataFormat<ResponsePaymentHistory.GetSummaryPaymentStatistics>>

    @Headers("Content-Type: application/json")
    @POST("/v0/mcht/name")
    suspend fun mchtName(
        @Body body: DataFormat<RequestUser.SearchMerchantId>
    ): Response<DataFormat<ResponseUser.SearchMerchantId>>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/statistics")
    fun statistics(
        @Header("Authorization") token: String?,
        @Body body: DataFormat<RequestPaymentHistory.GetPaymentStatistics>
    ): Response<DataFormat<ResponsePaymentHistory.GetPaymentStatistics>>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/list")
    suspend fun list(
        @Header("Authorization") token: String?,
        @Body body: DataFormat<RequestPaymentHistory.GetPaymentList>
    ): Response<DataFormat<ResponsePaymentHistory.GetPaymentList>>
}