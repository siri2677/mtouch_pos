package com.example.data.retrofitInterface

import android.os.Build
import com.example.data.apiService.PayAPIService
import com.example.data.apiService.SvcTmsAPIService
import com.skydoves.sandwich.adapters.ApiResponseCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiServiceRepositoryImpl {
    //    var BASE_URL = BuildConfig.BASE_URL
    private val BASE_API_URL = "https://svcapi.mtouch.com"
    private val BASE_SMS_URL = "https://sms.supersms.co:7020"

    fun getAPIService() : SvcTmsAPIService {
        var BASE_URL = "https://svctms.mtouch.com"
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
        return returnAPIService<SvcTmsAPIService>(BASE_URL, client)
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