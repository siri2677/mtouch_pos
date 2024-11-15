package com.example.mtouchpos.hilt

import android.content.Context
import com.example.domain.model.device.KsnetCardReaderRequestBuilder
import com.example.domain.model.device.KsnetCardReaderResponseBuilder
import com.example.domain.model.device.DeviceInfo
import com.example.domain.repositoryInterface.DeviceRepository
import com.example.domain.repositoryInterface.OfflinePaymentRepository
import com.example.domain.usecase.device.SearchBluetoothDevice
import com.example.domain.usecase.device.CommunicateKsnetCardReader
import com.example.domain.usecase.device.ConnectCardReader
import com.example.domain.usecase.device.FetchConnectedDeviceInfo
import com.example.domain.usecase.device.UpdateConnectedDeviceInfo
import com.example.domain.usecase.device.SearchUsbDevice
import com.example.domain.usecase.device.manager.CommunicateDeviceManager
import com.example.domain.usecase.device.manager.ConnectDeviceManager
import com.example.domain.usecase.offlinePayment.PushOfflineCancelPayment
import com.example.domain.usecase.offlinePayment.PushOfflinePayment
import com.example.domain.usecase.offlinePayment.RequestOfflineCancelPayment
import com.example.domain.usecase.offlinePayment.RequestOfflinePayment
import com.example.mtouchpos.viewmodel.usecasemanager.reader.DeviceCommunicateResponseDataImpl
import com.example.mtouchpos.viewmodel.usecasemanager.reader.bluetooth.SearchBluetooth
import com.example.mtouchpos.viewmodel.usecasemanager.reader.usb.SearchUsb
import com.example.mtouchpos.viewmodel.DeviceSettingViewModel
import com.google.gson.Gson
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
        RuntimeTypeAdapterFactory.of(DeviceInfo::class.java, "type")
            .registerSubtype(DeviceSettingViewModel.BluetoothDeviceInfo::class.java)
            .registerSubtype(DeviceSettingViewModel.UsbDeviceInfo::class.java)
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
        deviceConnectManager: ConnectDeviceManager,
        deviceCommunicateManager: CommunicateDeviceManager,
        deviceConnect: ConnectCardReader
    ): CommunicateKsnetCardReader = CommunicateKsnetCardReader(
        offlinePaymentRepository = offlinePaymentRepository,
        fetchConnectedDeviceInfo = fetchConnectedDeviceInfo,
        deviceOperationCallback = DeviceCommunicateResponseDataImpl,
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
        deviceOperationCallback = DeviceCommunicateResponseDataImpl
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