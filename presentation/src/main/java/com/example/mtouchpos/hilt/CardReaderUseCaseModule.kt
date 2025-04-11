package com.example.mtouchpos.hilt

import com.example.domain.model.cardreader.CardReaderData
import com.example.domain.model.cardreader.KsnetCardReaderRequestBuilder
import com.example.domain.model.cardreader.KsnetCardReaderResponseBuilder
import com.example.domain.repository.CardReaderCommunicateRepository
import com.example.domain.repository.DeviceRepository
import com.example.domain.usecase.cardreader.CommunicateKsnetCardReader
import com.example.domain.usecase.cardreader.ConnectCardReader
import com.example.domain.usecase.cardreader.DeleteDeviceInfo
import com.example.domain.usecase.cardreader.FetchConnectedDeviceInfo
import com.example.domain.usecase.cardreader.PrintCompletedTransaction
import com.example.domain.usecase.cardreader.UpdateConnectedDeviceInfo
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object CardReaderUseCaseModule {
    private val gsonBuilder = GsonBuilder().registerTypeAdapterFactory(
        RuntimeTypeAdapterFactory.of(CardReaderData::class.java, "type")
            .registerSubtype(CardReaderData.Bluetooth::class.java)
            .registerSubtype(CardReaderData.Usb::class.java)
    )

    @Provides
    @ViewModelScoped
    fun provideFetchConnectedDeviceInfoUseCase(
        deviceRepository: DeviceRepository
    ): FetchConnectedDeviceInfo = FetchConnectedDeviceInfo(
        deviceInfoAdapterFactoryGson = gsonBuilder.create(),
        deviceRepository = deviceRepository,
    )

    @Provides
    @ViewModelScoped
    fun provideDeleteDeviceInfoUseCase(
        deviceRepository: DeviceRepository
    ): DeleteDeviceInfo = DeleteDeviceInfo(deviceRepository = deviceRepository)

    @Provides
    @ViewModelScoped
    fun provideUpdateConnectedDeviceInfoUseCase(
        deviceRepository: DeviceRepository
    ): UpdateConnectedDeviceInfo = UpdateConnectedDeviceInfo(
        deviceInfoAdapterFactoryGson = gsonBuilder.create(),
        deviceRepository = deviceRepository
    )

    @Provides
    @ViewModelScoped
    fun provideDeviceCommunicateUseCase(
        cardReaderCommunicateRepository: CardReaderCommunicateRepository
    ): CommunicateKsnetCardReader = CommunicateKsnetCardReader(
        cardReaderCommunicateRepository = cardReaderCommunicateRepository,
        ksnetCardReaderResponseBuilder = KsnetCardReaderResponseBuilder(),
        ksnetCardReaderRequestBuilder = KsnetCardReaderRequestBuilder()
    )

    @Provides
    @ViewModelScoped
    fun provideConnectCardReaderUseCase(
        cardReaderCommunicateRepository: CardReaderCommunicateRepository
    ): ConnectCardReader = ConnectCardReader(
        cardReaderCommunicateRepository = cardReaderCommunicateRepository,
        ksnetCardReaderRequestBuilder = KsnetCardReaderRequestBuilder()
    )

    @Provides
    @ViewModelScoped
    fun providePrintCompletedTransaction(
        cardReaderCommunicateRepository: CardReaderCommunicateRepository
    ): PrintCompletedTransaction = PrintCompletedTransaction(
        cardReaderCommunicateRepository = cardReaderCommunicateRepository,
        ksnetCardReaderRequestBuilder = KsnetCardReaderRequestBuilder()
    )

}