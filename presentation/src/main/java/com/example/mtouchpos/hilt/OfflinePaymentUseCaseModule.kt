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
    fun provideDeviceCommunicateUseCase(
        cardReaderCommunicateRepository: CardReaderCommunicateRepository,
    ): CommunicateKsnetCardReader = CommunicateKsnetCardReader(
        cardReaderCommunicateRepository = cardReaderCommunicateRepository,
        ksnetCardReaderResponseBuilder = KsnetCardReaderResponseBuilder(),
        ksnetCardReaderRequestBuilder = KsnetCardReaderRequestBuilder()
    )

    @Provides
    @ViewModelScoped
    fun provideConnectTestCardReaderUseCase(
        cardReaderCommunicateRepository: CardReaderCommunicateRepository
    ): ConnectCardReader = ConnectCardReader(
        cardReaderCommunicateRepository = cardReaderCommunicateRepository,
        ksnetCardReaderRequestBuilder = KsnetCardReaderRequestBuilder()
    )

    @Provides
    @ViewModelScoped
    fun provideSocketCommunicateVanUseCase(
        offlinePaymentRepository: OfflinePaymentRepository
    ): KsnetSocketCommunicate = KsnetSocketCommunicate(offlinePaymentRepository)


//    @Provides
//    @ViewModelScoped
//    fun provideConnectBluetooth(
//        @ApplicationContext context: Context
//    ): ConnectBluetooth = ConnectBluetooth(context)
//
//    @Provides
//    @ViewModelScoped
//    fun provideConnectUsb(
//        @ApplicationContext context: Context
//    ): ConnectUsb = ConnectUsb(context)
}