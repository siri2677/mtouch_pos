package com.kwonps.mtouchpos.hilt

import com.kwonps.domain.repository.DirectPaymentRepository
import com.kwonps.domain.usecase.directPayment.RequestDirectCancelPayment
import com.kwonps.domain.usecase.directPayment.RequestDirectPayment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
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