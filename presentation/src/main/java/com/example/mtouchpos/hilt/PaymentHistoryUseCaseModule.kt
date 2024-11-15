package com.example.mtouchpos.hilt

import android.content.Context
import com.example.domain.repositoryInterface.PaymentHistoryRepository
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
        paymentHistoryRepository: PaymentHistoryRepository
    ): CheckDirectPayment = CheckDirectPayment(paymentHistoryRepository)

    @Provides
    @ViewModelScoped
    fun provideGetPaymentHistoryList(
        paymentHistoryRepository: PaymentHistoryRepository
    ): FetchPaymentHistoryList = FetchPaymentHistoryList(paymentHistoryRepository)

    @Provides
    @ViewModelScoped
    fun provideGetPaymentHistoryStatistics(
        paymentHistoryRepository: PaymentHistoryRepository
    ): FetchPaymentHistoryStatistics = FetchPaymentHistoryStatistics(paymentHistoryRepository)

    @Provides
    @ViewModelScoped
    fun provideGetSalesHistory(
        paymentHistoryRepository: PaymentHistoryRepository
    ): FetchSalesHistory = FetchSalesHistory(paymentHistoryRepository)
}