package com.example.data.remote.apiservice

import com.example.data.remote.dto.request.RequestDirectPayment
import com.example.data.remote.dto.response.ResponseDirectPayment
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST


interface PayAPIService {
    @Headers("Content-Type: application/json")
    @POST("/api/pay")
    suspend fun sendDirectPayment(
        @Header("Authorization") payKey: String,
        @Body body: RequestDirectPayment.DirectPayment
    ): Response<ResponseDirectPayment.DirectPayment>

    @Headers("Content-Type: application/json")
    @POST("/api/refund")
    suspend fun sendDirectRefund(
        @Header("Authorization") payKey: String,
        @Body body: RequestDirectPayment.DirectCancelPayment
    ): Response<ResponseDirectPayment.DirectCancelPayment>
}