package com.example.mtouchpos.hilt

import android.content.Context
import com.example.domain.model.device.KSNetCardReaderRequestBuilder
import com.example.domain.model.device.KSNetCardReaderResponseBuilder
import com.example.domain.model.device.DeviceInfo
import com.example.domain.usecase.device.SearchBluetoothDevice
import com.example.domain.usecase.device.CommunicateKsnetCardReader
import com.example.domain.usecase.device.ConnectCardReader
import com.example.domain.usecase.device.FetchConnectedDeviceInfo
import com.example.domain.usecase.device.UpdateConnectedDeviceInfo
import com.example.domain.usecase.device.SearchUsbDevice
import com.example.domain.usecase.offlinePayment.OfflineCancelPayment
import com.example.domain.usecase.offlinePayment.OfflinePayment
import com.example.mtouchpos.viewmodel.usecasemanager.reader.DeviceCommunicateResponseDataImpl
import com.example.mtouchpos.viewmodel.usecasemanager.reader.bluetooth.SearchBluetooth
import com.example.mtouchpos.viewmodel.usecasemanager.reader.usb.SearchUsb
import com.example.mtouchpos.viewmodel.DeviceSettingViewModel
import com.example.mtouchpos.viewmodel.usecasemanager.reader.bluetooth.ConnectBluetooth
import com.example.mtouchpos.viewmodel.usecasemanager.reader.usb.ConnectUsb
import com.example.mtouchpos.viewmodel.usecasemanager.terminal.CommunicatePM500
import com.example.mtouchpos.viewmodel.usecasemanager.terminal.CommunicateQ2
import com.example.mtouchpos.viewmodel.usecasemanager.terminal.CommunicateXPDA
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Provider

@Module
@InstallIn(ViewModelComponent::class)
object OfflinePaymentUseCaseModule {
    private val gsonBuilder = GsonBuilder().registerTypeAdapterFactory(
        RuntimeTypeAdapterFactory
            .of(DeviceInfo::class.java, "type")
            .registerSubtype(DeviceSettingViewModel.BluetoothDeviceInfo::class.java)
            .registerSubtype(DeviceSettingViewModel.UsbDeviceInfo::class.java)
    )

    @Provides
    @ViewModelScoped
    fun provideFetchConnectedDeviceInfoUseCase(
        @ApplicationContext context: Context
    ): FetchConnectedDeviceInfo = FetchConnectedDeviceInfo(
        deviceInfoAdapterFactoryGson = gsonBuilder.create(),
        deviceRepository = RepositoryModule.provideDeviceRepository(context),
    )

    @Provides
    @ViewModelScoped
    fun provideUpdateConnectedDeviceInfoUseCase(
        @ApplicationContext context: Context
    ): UpdateConnectedDeviceInfo = UpdateConnectedDeviceInfo(
        deviceInfoAdapterFactoryGson = gsonBuilder.create(),
        deviceRepository = RepositoryModule.provideDeviceRepository(context)
    )

    @Provides
    @ViewModelScoped
    fun provideOfflinePaymentUseCase(
        @ApplicationContext context: Context
    ): OfflinePayment = OfflinePayment(
        offlinePaymentRepository = RepositoryModule.provideOfflinePaymentRepository(context),
        communicateKsnetCardReader = provideDeviceCommunicateUseCase(context)
    )

    @Provides
    @ViewModelScoped
    fun provideOfflineCancelPaymentUseCase(
        @ApplicationContext context: Context
    ): OfflineCancelPayment = OfflineCancelPayment(
        offlinePaymentRepository = RepositoryModule.provideOfflinePaymentRepository(context),
        deviceCommunicate = provideDeviceCommunicateUseCase(context)
    )

    @Provides
    @ViewModelScoped
    fun provideDeviceCommunicateUseCase(
        @ApplicationContext context: Context
    ): CommunicateKsnetCardReader = CommunicateKsnetCardReader(
        offlinePaymentRepository = RepositoryModule.provideOfflinePaymentRepository(context),
        fetchConnectedDeviceInfo = provideFetchConnectedDeviceInfoUseCase(context),
        deviceOperationCallback = DeviceCommunicateResponseDataImpl,
        deviceConnectManager = RepositoryModule.provideDeviceConnectManager(context),
        deviceCommunicateManager = RepositoryModule.provideDeviceCommunicateManager(context),
        deviceConnect = provideDeviceConnectUseCase(),
        ksnetCardReaderResponseBuilder = KSNetCardReaderResponseBuilder(),
        ksnetCardReaderRequestBuilder =  KSNetCardReaderRequestBuilder()
    )

    @Provides
    @ViewModelScoped
    fun provideDeviceConnectUseCase(): ConnectCardReader = ConnectCardReader(
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