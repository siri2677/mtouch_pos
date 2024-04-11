package com.example.data.retrofit

import android.os.Build
import com.skydoves.sandwich.adapters.ApiResponseCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class ApiServiceImpl {
    private val BASE_API_URL = "https://svcapidev.mtouch.com"
    private var BASE_URL = "https://svctmsdev.mtouch.com"

    fun getAPIService() : TmsAPIService {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        return returnAPIService(
            url = BASE_URL,
            client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor{ chain: Interceptor.Chain ->
                    val request = chain.request()
                    val newRequest = request.newBuilder()
                        .header("User-Agent", "mtouch_new")
                        .build()
                    chain.proceed(newRequest)
                }
                .addInterceptor(interceptor)
                .build()
        )
    }

    fun getAPIDirectService(): PayAPIService {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .addInterceptor{ chain: Interceptor.Chain ->
                val request = chain.request()
                val newRequest = request.newBuilder()
                    .header("User-Agent", "KSR02_serial : " + Build.SERIAL + " / version: ")
                    .build()
                chain.proceed(newRequest)
            }
            .build()
        return returnAPIService<PayAPIService>(BASE_API_URL, client)
    }

    fun getSMSSendService(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build()
    }

    private inline fun <reified APIService> returnAPIService(url: String, client: OkHttpClient) : APIService {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ApiResponseCallAdapterFactory.create())
            .client(client)
            .build()
        return retrofit.create(APIService::class.java)
    }


}