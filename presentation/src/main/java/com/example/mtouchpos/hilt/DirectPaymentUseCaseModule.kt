package com.example.mtouchpos.hilt

import android.content.Context
import com.example.domain.repositoryInterface.DirectPaymentRepository
import com.example.domain.usecase.directPayment.RequestDirectCancelPayment
import com.example.domain.usecase.directPayment.RequestDirectPayment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object DirectPaymentUseCaseModule {
    @Provides
    @ViewModelScoped
    fun provideDirectPaymentUseCase(
        directPaymentRepository: DirectPaymentRepository
    ): RequestDirectPayment = RequestDirectPayment(directPaymentRepository)

    @Provides
    @ViewModelScoped
    fun provideDirectCancelPaymentUseCase(
        directPaymentRepository: DirectPaymentRepository
    ): RequestDirectCancelPayment = RequestDirectCancelPayment(directPaymentRepository)

}