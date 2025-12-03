package com.kwonps.mtouchpos.hilt

import com.kwonps.domain.repository.OfflinePaymentRepository
import com.kwonps.domain.usecase.offlinePayment.ProcessOfflinePayment
import com.kwonps.domain.usecase.offlinePayment.RequestOfflinePayment
import com.kwonps.domain.usecase.offlinePayment.SyncReceipt
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object OfflinePaymentUseCaseModule {
    @Provides
    @ViewModelScoped
    fun provideProcessOfflinePaymentUseCase(
        offlinePaymentRepository: OfflinePaymentRepository
    ): ProcessOfflinePayment = ProcessOfflinePayment(offlinePaymentRepository)

    @Provides
    @ViewModelScoped
    fun provideRequestOfflinePaymentUseCase(
        offlinePaymentRepository: OfflinePaymentRepository
    ): RequestOfflinePayment = RequestOfflinePayment(offlinePaymentRepository)

    @Provides
    @ViewModelScoped
    fun provideSyncReceiptUseCase(
        offlinePaymentRepository: OfflinePaymentRepository
    ): SyncReceipt = SyncReceipt(offlinePaymentRepository)

}