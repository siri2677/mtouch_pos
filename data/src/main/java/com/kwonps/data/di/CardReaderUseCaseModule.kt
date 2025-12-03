package com.kwonps.data.di

import com.kwonps.domain.adapter.JsonAdapter
import com.kwonps.domain.dispatcher.CoroutineDispatcherProvider
import com.kwonps.domain.model.cardreader.KsnetCardReaderRequestBuilder
import com.kwonps.domain.model.cardreader.KsnetCardReaderResponseBuilder
import com.kwonps.domain.repository.CardReaderCommunicateRepository
import com.kwonps.domain.repository.DeviceRepository
import com.kwonps.domain.usecase.cardreader.CommunicateKsnetCardReader
import com.kwonps.domain.usecase.cardreader.ConnectCardReader
import com.kwonps.domain.usecase.cardreader.DeleteDeviceInfo
import com.kwonps.domain.usecase.cardreader.FetchConnectedDeviceInfo
import com.kwonps.domain.usecase.cardreader.PrintCompletedTransaction
import com.kwonps.domain.usecase.cardreader.UpdateConnectedDeviceInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CardReaderUseCaseModule {
    @Provides
    @Singleton
    fun provideFetchConnectedDeviceInfoUseCase(
        jsonAdapter: JsonAdapter,
        deviceRepository: DeviceRepository,
        dispatcherProvider: CoroutineDispatcherProvider,
    ): FetchConnectedDeviceInfo = FetchConnectedDeviceInfo(
        jsonAdapter = jsonAdapter,
        deviceRepository = deviceRepository,
        dispatcherProvider = dispatcherProvider,
    )

    @Provides
    @Singleton
    fun provideDeleteDeviceInfoUseCase(
        deviceRepository: DeviceRepository
    ): DeleteDeviceInfo = DeleteDeviceInfo(deviceRepository = deviceRepository)

    @Provides
    @Singleton
    fun provideUpdateConnectedDeviceInfoUseCase(
        jsonAdapter: JsonAdapter,
        deviceRepository: DeviceRepository
    ): UpdateConnectedDeviceInfo = UpdateConnectedDeviceInfo(
        jsonAdapter = jsonAdapter,
        deviceRepository = deviceRepository
    )

    @Provides
    @Singleton
    fun provideDeviceCommunicateUseCase(
        cardReaderCommunicateRepository: CardReaderCommunicateRepository
    ): CommunicateKsnetCardReader = CommunicateKsnetCardReader(
        cardReaderCommunicateRepository = cardReaderCommunicateRepository,
        ksnetCardReaderResponseBuilder = KsnetCardReaderResponseBuilder(),
        ksnetCardReaderRequestBuilder = KsnetCardReaderRequestBuilder()
    )

    @Provides
    @Singleton
    fun provideConnectCardReaderUseCase(
        cardReaderCommunicateRepository: CardReaderCommunicateRepository
    ): ConnectCardReader = ConnectCardReader(
        cardReaderCommunicateRepository = cardReaderCommunicateRepository,
        ksnetCardReaderRequestBuilder = KsnetCardReaderRequestBuilder()
    )

    @Provides
    @Singleton
    fun providePrintCompletedTransaction(
        cardReaderCommunicateRepository: CardReaderCommunicateRepository
    ): PrintCompletedTransaction = PrintCompletedTransaction(
        cardReaderCommunicateRepository = cardReaderCommunicateRepository,
        ksnetCardReaderRequestBuilder = KsnetCardReaderRequestBuilder()
    )
}
