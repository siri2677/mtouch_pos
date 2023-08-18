package com.example.data.apiService

import com.mtouch.domain.model.tmsApiRequest.ListTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.StatisticsTmsRequestData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.ListTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.StatisticsTmsResponseData
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface GetPaymentInfomationAPIService {
    @Headers("Content-Type: application/json")
    @POST("/v0/trx/statistics")
    fun statistics(
        @Header("Authorization") token: String?,
        @Body body: StatisticsTmsRequestData
    ): Single<StatisticsTmsResponseData>

    @Headers("Content-Type: application/json")
    @POST("/v0/trx/list")
    fun list(
        @Header("Authorization") token: String?,
        @Body body: ListTmsRequestData
    ): Single<ListTmsResponseData>
}