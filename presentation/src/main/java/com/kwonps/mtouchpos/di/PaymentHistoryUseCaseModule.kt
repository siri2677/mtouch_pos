package com.kwonps.mtouchpos.di

import com.kwonps.domain.repository.PaymentHistoryRepository
import com.kwonps.domain.usecase.paymentHistory.CheckDirectPayment
import com.kwonps.domain.usecase.paymentHistory.FetchPaymentHistoryList
import com.kwonps.domain.usecase.paymentHistory.FetchPaymentHistoryStatistics
import com.kwonps.domain.usecase.paymentHistory.FetchSalesHistory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object PaymentHistoryUseCaseModule {
    @Provides
    @ActivityRetainedScoped
    fun provideCheckDirectPayment(
        paymentHistoryRepository: PaymentHistoryRepository
    ): CheckDirectPayment = CheckDirectPayment(paymentHistoryRepository)

    @Provides
    @ActivityRetainedScoped
    fun provideGetPaymentHistoryList(
        paymentHistoryRepository: PaymentHistoryRepository
    ): FetchPaymentHistoryList = FetchPaymentHistoryList(paymentHistoryRepository)

    @Provides
    @ActivityRetainedScoped
    fun provideGetPaymentHistoryStatistics(
        paymentHistoryRepository: PaymentHistoryRepository
    ): FetchPaymentHistoryStatistics = FetchPaymentHistoryStatistics(paymentHistoryRepository)

    @Provides
    @ActivityRetainedScoped
    fun provideGetSalesHistory(
        paymentHistoryRepository: PaymentHistoryRepository
    ): FetchSalesHistory = FetchSalesHistory(paymentHistoryRepository)
}