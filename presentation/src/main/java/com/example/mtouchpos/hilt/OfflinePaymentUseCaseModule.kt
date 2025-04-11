package com.example.mtouchpos.hilt

import com.example.domain.model.cardreader.KsnetCardReaderRequestBuilder
import com.example.domain.model.cardreader.KsnetCardReaderResponseBuilder
import com.example.domain.model.cardreader.CardReaderData
import com.example.domain.repository.DeviceRepository
import com.example.domain.repository.OfflinePaymentRepository
import com.example.domain.usecase.cardreader.CommunicateKsnetCardReader
import com.example.domain.usecase.cardreader.FetchConnectedDeviceInfo
import com.example.domain.usecase.cardreader.UpdateConnectedDeviceInfo
import com.example.domain.repository.CardReaderCommunicateRepository
import com.example.domain.usecase.cardreader.ConnectCardReader
import com.example.domain.usecase.cardreader.DeleteDeviceInfo
import com.example.domain.usecase.offlinePayment.KsnetSocketCommunicate
import com.example.domain.usecase.offlinePayment.PushOfflinePayment
import com.example.domain.usecase.offlinePayment.RequestOfflinePayment
import com.google.gson.GsonBuilder
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