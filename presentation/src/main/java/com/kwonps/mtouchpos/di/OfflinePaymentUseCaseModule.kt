package com.kwonps.mtouchpos.di

import com.kwonps.domain.repository.OfflinePaymentRepository
import com.kwonps.domain.usecase.offlinePayment.ProcessOfflinePayment
import com.kwonps.domain.usecase.offlinePayment.RequestOfflinePayment
import com.kwonps.domain.usecase.offlinePayment.SyncReceipt
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object OfflinePaymentUseCaseModule {
    @Provides
    @ActivityRetainedScoped
    fun provideProcessOfflinePaymentUseCase(
        offlinePaymentRepository: OfflinePaymentRepository
    ): ProcessOfflinePayment = ProcessOfflinePayment(offlinePaymentRepository)

    @Provides
    @ActivityRetainedScoped
    fun provideRequestOfflinePaymentUseCase(
        offlinePaymentRepository: OfflinePaymentRepository
    ): RequestOfflinePayment = RequestOfflinePayment(offlinePaymentRepository)

    @Provides
    @ActivityRetainedScoped
    fun provideSyncReceiptUseCase(
        offlinePaymentRepository: OfflinePaymentRepository
    ): SyncReceipt = SyncReceipt(offlinePaymentRepository)

}