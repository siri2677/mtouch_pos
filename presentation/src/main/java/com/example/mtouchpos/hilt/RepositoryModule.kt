package com.example.mtouchpos.hilt

import android.content.Context
import com.example.data.internal.UserInformationDatabase
import com.example.data.remote.RetrofitBuilder
import com.example.data.repositoryImpl.DeviceRepositoryImpl
import com.example.data.repositoryImpl.DirectPaymentRepositoryImpl
import com.example.data.repositoryImpl.OfflinePaymentRepositoryImpl
import com.example.data.repositoryImpl.PaymentHistoryRepositoryImpl
import com.example.data.repositoryImpl.UserRepositoryImpl
import com.example.domain.model.cardreader.CardReaderStatus
import com.example.domain.repository.DeviceRepository
import com.example.domain.repository.DirectPaymentRepository
import com.example.domain.repository.OfflinePaymentRepository
import com.example.domain.repository.PaymentHistoryRepository
import com.example.domain.repository.UserRepository
import com.example.domain.usecase.cardreader.FetchConnectedDeviceInfo
import com.example.domain.manager.cardreader.CardReaderCommunicateManager
import com.example.domain.manager.cardreader.CardReaderConnectManager
import com.example.domain.usecase.user.FetchConnectedUserInfo
import com.example.mtouchpos.managerImpl.cardreader.bluetooth.CommunicateBluetooth
import com.example.mtouchpos.managerImpl.cardreader.bluetooth.ConnectBluetooth
import com.example.mtouchpos.managerImpl.cardreader.usb.CommunicateUsb
import com.example.mtouchpos.managerImpl.cardreader.usb.ConnectUsb
import com.example.mtouchpos.viewmodel.CardReaderConnectVM
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex


@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
    private const val USER_INFORMATION = "userInformation"
    private const val DEVICE_INFORMATION = "deviceInformation"
    private const val SVC_TMS_URL = "https://svctms.mtouch.com"
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
    ): CardReaderCommunicateManager {
        val mutex = Mutex()
        val emptyDeviceCommunicateManager = object : CardReaderCommunicateManager {
            override fun bindingService() {}
            override fun unBindingService() {}
            override fun stopRetry(byteArray: ByteArray) {}
            override fun connect(deviceInfo: String) {}
            override fun sendData(byteArray: ByteArray) {}
//            override fun isDeviceServiceInitialized(): Boolean = false
        }

        return provideDeviceRepository(context).getDeviceInformation()?.let {
            when(fetchConnectedDeviceInfo()) {
                is CardReaderConnectVM.BluetoothDeviceInfo -> CommunicateBluetooth(context, mutex)
                is CardReaderConnectVM.UsbDeviceInfo -> CommunicateUsb(context, mutex)
                else -> emptyDeviceCommunicateManager
            }
        } ?: emptyDeviceCommunicateManager
    }

    @Provides
    @ViewModelScoped
    fun provideDeviceConnectManager(
        @ApplicationContext context: Context,
        fetchConnectedDeviceInfo: FetchConnectedDeviceInfo
    ): CardReaderConnectManager {
        val emptyDeviceConnectManager = object: CardReaderConnectManager {
            override fun connect(deviceInfo: String) {}
            override fun disConnect() {}
        }

        return provideDeviceRepository(context).getDeviceInformation()?.let {
            when(fetchConnectedDeviceInfo()) {
                is CardReaderConnectVM.BluetoothDeviceInfo -> ConnectBluetooth(context)
                is CardReaderConnectVM.UsbDeviceInfo -> ConnectUsb(context)
                else -> emptyDeviceConnectManager
            }
        } ?: emptyDeviceConnectManager
    }
}