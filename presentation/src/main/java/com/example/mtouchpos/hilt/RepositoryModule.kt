package com.example.mtouchpos.hilt

import android.content.Context
import com.example.data.internal.UserInformationDatabase
import com.example.data.remote.RetrofitBuilder
import com.example.data.repository.DeviceRepositoryImpl
import com.example.data.repository.DirectPaymentRepositoryImpl
import com.example.data.repository.OfflinePaymentRepositoryImpl
import com.example.data.repository.PaymentHistoryRepositoryImpl
import com.example.data.repository.UserRepositoryImpl
import com.example.domain.repositoryInterface.DeviceRepository
import com.example.domain.repositoryInterface.DirectPaymentRepository
import com.example.domain.repositoryInterface.OfflinePaymentRepository
import com.example.domain.repositoryInterface.PaymentHistoryRepository
import com.example.domain.repositoryInterface.UserRepository
import com.example.domain.usecase.device.manager.CommunicateDeviceManager
import com.example.domain.usecase.device.manager.ConnectDeviceManager
import com.example.mtouchpos.viewmodel.usecasemanager.reader.bluetooth.CommunicateBluetooth
import com.example.mtouchpos.viewmodel.usecasemanager.reader.bluetooth.ConnectBluetooth
import com.example.mtouchpos.viewmodel.usecasemanager.reader.usb.CommunicateUsb
import com.example.mtouchpos.viewmodel.usecasemanager.reader.usb.ConnectUsb
import com.example.mtouchpos.viewmodel.DeviceSettingViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
    private val USER_INFORMATION = "userInformation"
    private val DEVICE_INFORMATION = "deviceInformation"
    private val SVC_TMS_URL = "https://svctms.mtouch.com"
    private val SVC_API_URL = "https://svcapidev.mtouch.com"

    @Provides
    @ViewModelScoped
    fun provideUserRepository(
        @ApplicationContext context: Context
    ): UserRepository = USER_INFORMATION.let {
        UserRepositoryImpl(
            apiService = RetrofitBuilder().getTmsAPIService(SVC_TMS_URL),
            userInformationDao = UserInformationDatabase.getInstance(context).userInformationDao(),
            sharedPreferences = context.getSharedPreferences(it, Context.MODE_PRIVATE),
            key = it
        )
    }

    @Provides
    @ViewModelScoped
    fun provideOfflinePaymentRepository(
        @ApplicationContext context: Context
    ): OfflinePaymentRepository = OfflinePaymentRepositoryImpl(
        apiService = RetrofitBuilder().getTmsAPIService(SVC_TMS_URL),
        token = UserUseCaseModule.provideFetchConnectedUserInfo(context)()?.key ?: ""
    )

    @Provides
    @ViewModelScoped
    fun provideDeviceRepository(
        @ApplicationContext context: Context
    ): DeviceRepository = DEVICE_INFORMATION.let {
        DeviceRepositoryImpl(
            sharedPreferences = context.getSharedPreferences(it, Context.MODE_PRIVATE),
            key = it
        )
    }

    @Provides
    @ViewModelScoped
    fun provideDirectPaymentRepository(
        @ApplicationContext context: Context
    ): DirectPaymentRepository = DirectPaymentRepositoryImpl(
        apiService = RetrofitBuilder().getPayAPIService(SVC_API_URL),
        payKey = UserUseCaseModule.provideFetchConnectedUserInfo(context)()?.payKey ?: ""
    )

    @Provides
    @ViewModelScoped
    fun providePaymentHistoryRepository(
        @ApplicationContext context: Context
    ): PaymentHistoryRepository = PaymentHistoryRepositoryImpl(
        apiService = RetrofitBuilder().getTmsAPIService(SVC_TMS_URL),
        token = UserUseCaseModule.provideFetchConnectedUserInfo(context)()?.key ?: ""
    )

    @Provides
    @ViewModelScoped
    fun provideDeviceCommunicateManager(
        @ApplicationContext context: Context
    ): CommunicateDeviceManager {
        val emptyDeviceCommunicateManager = object : CommunicateDeviceManager {
            override fun bindingService() {}
            override fun unBindingService() {}
            override fun sendData(byteArray: ByteArray) {}
            override fun isDeviceServiceInitialized(): Boolean = false
        }

        return provideDeviceRepository(context).getDeviceInformation()?.let {
            when(OfflinePaymentUseCaseModule.provideFetchConnectedDeviceInfoUseCase(context)()) {
                is DeviceSettingViewModel.BluetoothDeviceInfo -> CommunicateBluetooth(context)
                is DeviceSettingViewModel.UsbDeviceInfo -> CommunicateUsb(context)
                else -> emptyDeviceCommunicateManager
            }
        } ?: emptyDeviceCommunicateManager
    }

    @Provides
    @ViewModelScoped
    fun provideDeviceConnectManager(
        @ApplicationContext context: Context
    ): ConnectDeviceManager {
        val emptyDeviceConnectManager = object: ConnectDeviceManager {
            override fun connect(deviceInfo: String) {}
            override fun disConnect() {}
        }

        return provideDeviceRepository(context).getDeviceInformation()?.let {
            when(OfflinePaymentUseCaseModule.provideFetchConnectedDeviceInfoUseCase(context)()) {
                is DeviceSettingViewModel.BluetoothDeviceInfo -> ConnectBluetooth(context)
                is DeviceSettingViewModel.UsbDeviceInfo -> ConnectUsb(context)
                else -> emptyDeviceConnectManager
            }
        } ?: emptyDeviceConnectManager
    }
}