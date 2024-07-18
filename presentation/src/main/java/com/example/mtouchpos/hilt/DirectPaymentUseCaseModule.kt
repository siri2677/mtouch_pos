package com.example.mtouchpos.hilt

import android.content.Context
import com.example.domain.usecase.directPayment.DirectCancelPayment
import com.example.domain.usecase.directPayment.DirectPayment
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
    ): DirectPayment = DirectPayment(RepositoryModule.provideDirectPaymentRepository(context))

    @Provides
    @ViewModelScoped
    fun provideDirectCancelPaymentUseCase(
        @ApplicationContext context: Context
    ): DirectCancelPayment = DirectCancelPayment(RepositoryModule.provideDirectPaymentRepository(context))

}