package com.example.data.retrofit

import com.example.domain.model.request.pay.RequestPayModel
import com.example.domain.model.response.pay.ResponsePayModel
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST


interface PayAPIService {
    @Headers("Content-Type: application/json")
    @POST("/api/pay")
    suspend fun sendDirectPayment(
        @Header("Authorization") payKey: String,
        @Body body: RequestPayModel.DirectPayment
    ): ApiResponse<ResponsePayModel.DirectPayment>

    @Headers("Content-Type: application/json")
    @POST("/api/refund")
    suspend fun sendDirectRefund(
        @Header("Authorization") payKey: String,
        @Body body: RequestPayModel.DirectCancelPayment
    ): ApiResponse<ResponsePayModel.DirectCancelPayment>
}