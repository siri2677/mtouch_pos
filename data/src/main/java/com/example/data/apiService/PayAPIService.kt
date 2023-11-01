package com.example.data.apiService

import com.example.data.entity.api.request.pay.RequestDirectCancelPaymentEntity
import com.example.data.entity.api.request.pay.RequestDirectPaymentEntity
import com.example.data.entity.api.response.pay.ResponseDirectCancelPaymentEntity
import com.example.data.entity.api.response.pay.ResponseDirectPaymentEntity
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
        @Body body: RequestDirectPaymentEntity
    ): ApiResponse<ResponseDirectPaymentEntity>

    @Headers("Content-Type: application/json")
    @POST("/api/refund")
    suspend fun sendDirectRefund(
        @Header("Authorization") payKey: String,
        @Body body: RequestDirectCancelPaymentEntity
    ): ApiResponse<ResponseDirectCancelPaymentEntity>
}