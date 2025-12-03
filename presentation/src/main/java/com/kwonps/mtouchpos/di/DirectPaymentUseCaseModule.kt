package com.kwonps.mtouchpos.di

import com.kwonps.domain.repository.DirectPaymentRepository
import com.kwonps.domain.usecase.directPayment.RequestDirectCancelPayment
import com.kwonps.domain.usecase.directPayment.RequestDirectPayment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object DirectPaymentUseCaseModule {
    @Provides
    @ActivityRetainedScoped
    fun provideDirectPaymentUseCase(
        directPaymentRepository: DirectPaymentRepository
    ): RequestDirectPayment = RequestDirectPayment(directPaymentRepository)

    @Provides
    @ActivityRetainedScoped
    fun provideDirectCancelPaymentUseCase(
        directPaymentRepository: DirectPaymentRepository
    ): RequestDirectCancelPayment = RequestDirectCancelPayment(directPaymentRepository)

}