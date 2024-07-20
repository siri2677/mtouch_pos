package com.example.mtouchpos.hilt

import android.content.Context
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
        @ApplicationContext context: Context
    ): RequestDirectPayment = RequestDirectPayment(RepositoryModule.provideDirectPaymentRepository(context))

    @Provides
    @ViewModelScoped
    fun provideDirectCancelPaymentUseCase(
        @ApplicationContext context: Context
    ): RequestDirectCancelPayment = RequestDirectCancelPayment(RepositoryModule.provideDirectPaymentRepository(context))

}