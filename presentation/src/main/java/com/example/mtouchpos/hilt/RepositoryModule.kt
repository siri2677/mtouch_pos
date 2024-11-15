package com.example.mtouchpos.hilt

import android.content.Context
import com.example.data.internal.UserInformationDatabase
import com.example.data.remote.RetrofitBuilder
import com.example.data.repository.DeviceRepositoryImpl
import com.example.data.repository.DirectPaymentRepositoryImpl
import com.example.data.repository.OfflinePaymentRepositoryImpl
import com.example.data.repository.PaymentHistoryRepositoryImpl
import com.example.data.repository.UserRepositoryImpl
import com.example.domain.model.device.DeviceConnectStatus
import com.example.domain.repositoryInterface.DeviceRepository
import com.example.domain.repositoryInterface.DirectPaymentRepository
import com.example.domain.repositoryInterface.OfflinePaymentRepository
import com.example.domain.repositoryInterface.PaymentHistoryRepository
import com.example.domain.repositoryInterface.UserRepository
import com.example.domain.usecase.device.FetchConnectedDeviceInfo
import com.example.domain.usecase.device.manager.CommunicateDeviceManager
import com.example.domain.usecase.device.manager.ConnectDeviceManager
import com.example.domain.usecase.user.FetchConnectedUserInfo
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex


@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
    private const val USER_INFORMATION = "userInformation"
    private const val DEVICE_INFORMATION = "deviceInformation"
    private const val SVC_TMS_URL = "https://svctmsdev.mtouch.com"
    private const val SVC_API_URL = "https://svcapi.mtouch.com"

    @Provides
    @ViewModelScoped
    fun provideUserRepository(
        @ApplicationContext context: Context
    ): UserRepository = UserRepositoryImpl(
        apiService = RetrofitBuilder.getTmsAPIService(SVC_TMS_URL),
        userInformationDao = UserInformationDatabase.getInstance(context).userInformationDao(),
        sharedPreferences = context.getSharedPreferences(USER_INFORMATION, Context.MODE_PRIVATE),
        key = USER_INFORMATION
    )

    @Provides
    @ViewModelScoped
    fun provideOfflinePaymentRepository(
        fetchConnectedUserInfo: FetchConnectedUserInfo
    ): OfflinePaymentRepository = OfflinePaymentRepositoryImpl(
        apiService = RetrofitBuilder.getTmsAPIService(SVC_TMS_URL),
        token = fetchConnectedUserInfo()?.key ?: ""
    )

    @Provides
    @ViewModelScoped
    fun provideDeviceRepository(
        @ApplicationContext context: Context
    ): DeviceRepository = DeviceRepositoryImpl(
        sharedPreferences = context.getSharedPreferences(DEVICE_INFORMATION, Context.MODE_PRIVATE),
        key = DEVICE_INFORMATION
    )

    @Provides
    @ViewModelScoped
    fun provideDirectPaymentRepository(
        fetchConnectedUserInfo: FetchConnectedUserInfo
    ): DirectPaymentRepository = DirectPaymentRepositoryImpl(
        apiService = RetrofitBuilder.getPayAPIService(SVC_API_URL),
        payKey = fetchConnectedUserInfo()?.payKey ?: ""
    )

    @Provides
    @ViewModelScoped
    fun providePaymentHistoryRepository(
        fetchConnectedUserInfo: FetchConnectedUserInfo
    ): PaymentHistoryRepository = PaymentHistoryRepositoryImpl(
        apiService = RetrofitBuilder.getTmsAPIService(SVC_TMS_URL),
        token = fetchConnectedUserInfo()?.key ?: ""
    )

    @Provides
    @ViewModelScoped
    fun provideDeviceCommunicateManager(
        @ApplicationContext context: Context,
        fetchConnectedDeviceInfo: FetchConnectedDeviceInfo
    ): CommunicateDeviceManager {
        val mutex = Mutex()
        val emptyDeviceCommunicateManager = object : CommunicateDeviceManager {
            override fun bindingService() {}
            override fun unBindingService() {}
            override fun connect(deviceInfo: String) = flow {
                emit(DeviceConnectStatus.DisConnected)
            }
            override fun sendData(byteArray: ByteArray) {}
            override fun isDeviceServiceInitialized(): Boolean = false
        }

        return provideDeviceRepository(context).getDeviceInformation()?.let {
            when(fetchConnectedDeviceInfo()) {
                is DeviceSettingViewModel.BluetoothDeviceInfo -> CommunicateBluetooth(context, mutex)
                is DeviceSettingViewModel.UsbDeviceInfo -> CommunicateUsb(context, mutex)
                else -> emptyDeviceCommunicateManager
            }
        } ?: emptyDeviceCommunicateManager
    }

    @Provides
    @ViewModelScoped
    fun provideDeviceConnectManager(
        @ApplicationContext context: Context,
        fetchConnectedDeviceInfo: FetchConnectedDeviceInfo
    ): ConnectDeviceManager {
        val emptyDeviceConnectManager = object: ConnectDeviceManager {
            override fun connect(deviceInfo: String) {}
            override fun disConnect() {}
        }

        return provideDeviceRepository(context).getDeviceInformation()?.let {
            when(fetchConnectedDeviceInfo()) {
                is DeviceSettingViewModel.BluetoothDeviceInfo -> ConnectBluetooth(context)
                is DeviceSettingViewModel.UsbDeviceInfo -> ConnectUsb(context)
                else -> emptyDeviceConnectManager
            }
        } ?: emptyDeviceConnectManager
    }
}