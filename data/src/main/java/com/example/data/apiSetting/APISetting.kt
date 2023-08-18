package com.example.data.apiSetting

import android.os.Build
import com.example.data.apiService.SmsRelatedAPIService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class APISetting {
    inline fun <reified APIService> getAPIService(): APIService  {
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
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(client)
            .build()
        return retrofit.create(APIService::class.java)
    }

    inline fun <reified APIService> getAPIDirectService(): APIService {
        var BASE_URL = "https://svcapi.mtouch.com"
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
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(client)
            .build()
        return retrofit.create(APIService::class.java)
    }

    fun getSMSSendService(): SmsRelatedAPIService {
        val BASE_SMS_URL = "https://sms.supersms.co:7020"
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_SMS_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(client)
            .build()
        return retrofit.create(SmsRelatedAPIService::class.java)
    }

}

