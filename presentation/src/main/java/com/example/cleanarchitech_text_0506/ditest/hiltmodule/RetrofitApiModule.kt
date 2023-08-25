package com.example.cleanarchitech_text_0506.ditest.hiltmodule

import android.os.Build
import com.example.data.apiService.GetPaymentInformationAPIService
import com.example.data.apiService.LoginRelatedAPIService
import com.example.data.apiService.PaymentProgressingAPIService
import com.example.data.apiService.SmsRelatedAPIService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier


@Module
@InstallIn(ViewModelComponent::class)
object RetrofitApiModule {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class Auth
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class Setting

    @Provides
    @ViewModelScoped
    fun provideOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
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
    }
    @Provides
    @ViewModelScoped
    @Auth
    fun retrofitAuth(
        client: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://svctms.mtouch.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(client)
            .build()

    @Provides
    @ViewModelScoped
    @Setting
    fun retrofitSetting(
        client: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://svcapi.mtouch.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(client)
            .build()

    @Provides
    @ViewModelScoped
    fun getPaymentInformationAPIService(@Auth retrofit: Retrofit): GetPaymentInformationAPIService = retrofit.create(GetPaymentInformationAPIService::class.java)
    @Provides
    @ViewModelScoped
    fun loginRelatedAPIService(@Auth retrofit: Retrofit): LoginRelatedAPIService = retrofit.create(LoginRelatedAPIService::class.java)
    @Provides
    @ViewModelScoped
    fun paymentProgressingAPIService(@Auth retrofit: Retrofit): PaymentProgressingAPIService = retrofit.create(PaymentProgressingAPIService::class.java)
    @Provides
    @ViewModelScoped
    fun smsRelatedAPIService(@Auth retrofit: Retrofit): SmsRelatedAPIService = retrofit.create(SmsRelatedAPIService::class.java)

}