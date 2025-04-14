package com.kwonps.mtouchpos.hilt

import com.kwonps.domain.repository.OfflinePaymentRepository
import com.kwonps.domain.usecase.offlinePayment.KsnetSocketCommunicate
import com.kwonps.domain.usecase.offlinePayment.PushOfflinePayment
import com.kwonps.domain.usecase.offlinePayment.RequestOfflinePayment
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
    fun provideOfflinePaymentUseCase(
        offlinePaymentRepository: OfflinePaymentRepository
    ): RequestOfflinePayment = RequestOfflinePayment(offlinePaymentRepository)

    @Provides
    @ViewModelScoped
    fun providePushOfflinePaymentUseCase(
        offlinePaymentRepository: OfflinePaymentRepository
    ): PushOfflinePayment = PushOfflinePayment(offlinePaymentRepository)

    @Provides
    @ViewModelScoped
    fun provideSocketCommunicateVanUseCase(
        offlinePaymentRepository: OfflinePaymentRepository
    ): KsnetSocketCommunicate = KsnetSocketCommunicate(offlinePaymentRepository)

}