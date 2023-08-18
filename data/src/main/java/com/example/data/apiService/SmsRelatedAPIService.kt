package com.example.data.apiService

import io.reactivex.rxjava3.core.Single
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PartMap
import java.util.HashMap

interface SmsRelatedAPIService {
    @Headers("Content-Type: application/json")
    @POST("/v0/sms/send")
    suspend fun smsSendMessage(
        @Body body: Request?
    ): Single<ResponseBody>

    @Headers("Content-Type: application/json", "Accept: application/json", "Accept-Language: ko_KR")
    @POST("/sms/v3/multiple-destinations")
    fun sendSMS(
        @Header("Authorization") token: String?,
        @Body body: HashMap<*, *>?
    ): Single<ResponseBody>

    @Multipart //    @Headers({"Content-Type: multipart/form-data; boundary=\"mtouch_sms_file\"","Accept: application/json","Accept-Language: ko_KR"})
    @POST("/sms/v3/file")
    fun uploadReceipt(
        @Header("Authorization") token: String?,
        @PartMap params: Map<String?, RequestBody?>? //                                     @Part("file\"; filename=\"receipt.jpeg\" ") RequestBody file
    ): Single<ResponseBody>
}