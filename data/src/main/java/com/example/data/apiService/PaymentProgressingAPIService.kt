package com.example.data.apiService

import com.mtouch.domain.model.tmsApiRequest.CRuleTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.CheckTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.PushTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.RuleTmsRequestData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.CRuleTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.CheckTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.PushTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.RuleTmsResponseData
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface PaymentProgressingAPIService {
    @Headers("Content-Type: application/json")
    @POST("/v0/trx/rule")
    fun rule(
        @Header("Authorization") token: String?,
        @Body body: RuleTmsRequestData
    ): Single<RuleTmsResponseData>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/crule")
    fun crule(
        @Header("Authorization") token: String?,
        @Body body: CRuleTmsRequestData
    ): Single<CRuleTmsResponseData>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/push")
    fun push(
        @Header("Authorization") token: String?,
        @Body body: PushTmsRequestData
    ): Single<PushTmsResponseData>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/check")
    fun check(
        @Header("Authorization") token: String?,
        @Body body: CheckTmsRequestData
    ): Single<CheckTmsResponseData>

}