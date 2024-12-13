package com.example.mtouchpos.hilt

import android.content.Context
import com.example.domain.model.cardreader.KsnetCardReaderRequestBuilder
import com.example.domain.model.cardreader.KsnetCardReaderResponseBuilder
import com.example.domain.model.cardreader.CardReaderData
import com.example.domain.repository.DeviceRepository
import com.example.domain.repository.OfflinePaymentRepository
import com.example.domain.usecase.cardreader.SearchBluetoothDevice
import com.example.domain.usecase.cardreader.CommunicateKsnetCardReader
import com.example.domain.usecase.cardreader.ConnectCardReader
import com.example.domain.usecase.cardreader.FetchConnectedDeviceInfo
import com.example.domain.usecase.cardreader.UpdateConnectedDeviceInfo
import com.example.domain.usecase.cardreader.SearchUsbDevice
import com.example.domain.manager.cardreader.CardReaderCommunicateManager
import com.example.domain.manager.cardreader.CardReaderConnectManager
import com.example.domain.usecase.offlinePayment.PushOfflineCancelPayment
import com.example.domain.usecase.offlinePayment.PushOfflinePayment
import com.example.domain.usecase.offlinePayment.RequestOfflineCancelPayment
import com.example.domain.usecase.offlinePayment.RequestOfflinePayment
import com.example.mtouchpos.managerImpl.cardreader.CardReaderResponseImpl
import com.example.mtouchpos.managerImpl.cardreader.bluetooth.SearchBluetooth
import com.example.mtouchpos.managerImpl.cardreader.usb.SearchUsb
import com.example.mtouchpos.viewmodel.CardReaderConnectVM
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.sync.Mutex

@Module
@InstallIn(ViewModelComponent::class)
object OfflinePaymentUseCaseModule {
    private val gsonBuilder = GsonBuilder().registerTypeAdapterFactory(
        RuntimeTypeAdapterFactory.of(CardReaderData::class.java, "type")
            .registerSubtype(CardReaderConnectVM.BluetoothDeviceInfo::class.java)
            .registerSubtype(CardReaderConnectVM.UsbDeviceInfo::class.java)
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
    fun provideUpdateConnectedDeviceInfoUseCase(
        deviceRepository: DeviceRepository
    ): UpdateConnectedDeviceInfo = UpdateConnectedDeviceInfo(
        deviceInfoAdapterFactoryGson = gsonBuilder.create(),
        deviceRepository = deviceRepository
    )

    @Provides
    @ViewModelScoped
    fun provideOfflinePaymentUseCase(
        offlinePaymentRepository: OfflinePaymentRepository,
        communicateKsnetCardReader: CommunicateKsnetCardReader
    ): RequestOfflinePayment = RequestOfflinePayment(
        offlinePaymentRepository = offlinePaymentRepository,
        communicateKsnetCardReader = communicateKsnetCardReader
    )

    @Provides
    @ViewModelScoped
    fun provideOfflineCancelPaymentUseCase(
        offlinePaymentRepository: OfflinePaymentRepository,
        communicateKsnetCardReader: CommunicateKsnetCardReader
    ): RequestOfflineCancelPayment = RequestOfflineCancelPayment(
        offlinePaymentRepository = offlinePaymentRepository,
        communicateKsnetCardReader = communicateKsnetCardReader
    )

    @Provides
    @ViewModelScoped
    fun providePushOfflinePaymentUseCase(
        offlinePaymentRepository: OfflinePaymentRepository
    ): PushOfflinePayment = PushOfflinePayment(
        offlinePaymentRepository = offlinePaymentRepository,
    )

    @Provides
    @ViewModelScoped
    fun providePushOfflineCancelPaymentUseCase(
        offlinePaymentRepository: OfflinePaymentRepository
    ): PushOfflineCancelPayment = PushOfflineCancelPayment(
        offlinePaymentRepository = offlinePaymentRepository,
    )

    @Provides
    @ViewModelScoped
    fun provideDeviceCommunicateUseCase(
        offlinePaymentRepository: OfflinePaymentRepository,
        fetchConnectedDeviceInfo: FetchConnectedDeviceInfo,
        deviceConnectManager: CardReaderConnectManager,
        deviceCommunicateManager: CardReaderCommunicateManager,
        deviceConnect: ConnectCardReader
    ): CommunicateKsnetCardReader = CommunicateKsnetCardReader(
        offlinePaymentRepository = offlinePaymentRepository,
        fetchConnectedDeviceInfo = fetchConnectedDeviceInfo,
        deviceOperationCallback = CardReaderResponseImpl,
        deviceConnectManager = deviceConnectManager,
        deviceCommunicateManager = deviceCommunicateManager,
        deviceConnect = deviceConnect,
        ksnetCardReaderResponseBuilder = KsnetCardReaderResponseBuilder(),
        ksnetCardReaderRequestBuilder =  KsnetCardReaderRequestBuilder()
    )

    @Provides
    @ViewModelScoped
    fun provideDeviceConnectUseCase(): ConnectCardReader = ConnectCardReader(
        mutex = Mutex(),
        deviceOperationCallback = CardReaderResponseImpl
    )

    @Provides
    @ViewModelScoped
    fun provideBluetoothDeviceSearchUseCase(
        @ApplicationContext context: Context
    ): SearchBluetoothDevice = SearchBluetoothDevice(SearchBluetooth(context))

    @Provides
    @ViewModelScoped
    fun provideUsbDeviceSearchUseCase(
        @ApplicationContext context: Context
    ): SearchUsbDevice = SearchUsbDevice(SearchUsb(context))

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