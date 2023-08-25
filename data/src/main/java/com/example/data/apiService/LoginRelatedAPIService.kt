package com.example.data.apiService

import com.mtouch.domain.model.tmsApiRequest.KeyTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.MchtNameTmsRequestData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.KeyTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.MchtNameTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.SummaryTmsResponseData
import io.reactivex.rxjava3.core.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginRelatedAPIService {
    @Headers("Content-Type: application/json")
    @GET("/v0/key")
    fun key(
        @Header("Authorization") token: String?
    ): Single<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/v0/key")
    suspend fun key(
        @Body body: KeyTmsRequestData
    ): Response<KeyTmsResponseData>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/summary")
    suspend fun summary(
        @Header("Authorization") token: String?
    ): Response<SummaryTmsResponseData>

    @Headers("Content-Type: application/json")
    @POST("/v0/mcht/name")
    suspend fun mchtName(
        @Body body: MchtNameTmsRequestData
    ): Response<MchtNameTmsResponseData>
}