package com.example.mtouchpos.hilt

import android.content.Context
import com.example.domain.usecase.paymentHistory.CheckDirectPayment
import com.example.domain.usecase.paymentHistory.FetchPaymentHistoryList
import com.example.domain.usecase.paymentHistory.FetchPaymentHistoryStatistics
import com.example.domain.usecase.paymentHistory.FetchSalesHistory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object PaymentHistoryUseCaseModule {
    @Provides
    @ViewModelScoped
    fun provideCheckDirectPayment(
        @ApplicationContext context: Context
    ): CheckDirectPayment = CheckDirectPayment(RepositoryModule.providePaymentHistoryRepository(context))

    @Provides
    @ViewModelScoped
    fun provideGetPaymentHistoryList(
        @ApplicationContext context: Context
    ): FetchPaymentHistoryList = FetchPaymentHistoryList(RepositoryModule.providePaymentHistoryRepository(context))

    @Provides
    @ViewModelScoped
    fun provideGetPaymentHistoryStatistics(
        @ApplicationContext context: Context
    ): FetchPaymentHistoryStatistics = FetchPaymentHistoryStatistics(RepositoryModule.providePaymentHistoryRepository(context))
    @Provides
    @ViewModelScoped
    fun provideGetSalesHistory(
        @ApplicationContext context: Context
    ): FetchSalesHistory = FetchSalesHistory(RepositoryModule.providePaymentHistoryRepository(context))
}