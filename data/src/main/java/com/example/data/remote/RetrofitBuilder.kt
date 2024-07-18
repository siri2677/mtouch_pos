package com.example.data.remote

import android.os.Build
import com.example.data.remote.apiservice.PayAPIService
import com.example.data.remote.apiservice.TmsAPIService
import com.skydoves.sandwich.adapters.ApiResponseCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitBuilder {
    private val interceptor = HttpLoggingInterceptor()

    fun getTmsAPIService(url: String): TmsAPIService = getAPIService(
        url = url,
        client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(interceptor.also { it.level = HttpLoggingInterceptor.Level.BODY })
            .addInterceptor{ chain: Interceptor.Chain ->
                val request = chain.request()
                val newRequest = request.newBuilder()
                    .header("User-Agent", "mtouch_new")
                    .build()
                chain.proceed(newRequest)
            }
            .build()
    )

    fun getPayAPIService(url: String): PayAPIService = getAPIService(
        url = url,
        client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(interceptor.also { it.level = HttpLoggingInterceptor.Level.BODY })
            .addInterceptor{ chain: Interceptor.Chain ->
                val request = chain.request()
                val newRequest = request.newBuilder()
                    .header("User-Agent", "KSR02_serial : " + Build.SERIAL + " / version: ")
                    .build()
                chain.proceed(newRequest)
            }
            .build()
    )


    fun getSMSSendService() = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(interceptor.also { it.level = HttpLoggingInterceptor.Level.BODY })
        .build()

    private inline fun <reified APIService> getAPIService(
        url: String,
        client: OkHttpClient
    ): APIService = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create(APIService::class.java)
}