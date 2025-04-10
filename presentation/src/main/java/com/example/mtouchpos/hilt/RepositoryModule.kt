package com.example.mtouchpos.hilt

import android.content.Context
import com.example.data.internal.DatabaseHelper
import com.example.data.remote.RetrofitBuilder
import com.example.data.repositoryImpl.BluetoothCardReaderRepositoryImpl
import com.example.data.service.BluetoothCardReaderService
import com.example.data.repositoryImpl.DeviceRepositoryImpl
import com.example.data.repositoryImpl.DirectPaymentRepositoryImpl
import com.example.data.repositoryImpl.OfflinePaymentRepositoryImpl
import com.example.data.repositoryImpl.PaymentHistoryRepositoryImpl
import com.example.data.repositoryImpl.UsbCardReaderRepositoryImpl
import com.example.data.service.UsbCardReaderService
import com.example.data.repositoryImpl.UserRepositoryImpl
import com.example.domain.model.cardreader.CardReaderData
import com.example.domain.model.cardreader.CardReaderStatus
import com.example.domain.repository.DeviceRepository
import com.example.domain.repository.DirectPaymentRepository
import com.example.domain.repository.OfflinePaymentRepository
import com.example.domain.repository.PaymentHistoryRepository
import com.example.domain.repository.UserRepository
import com.example.domain.usecase.cardreader.FetchConnectedDeviceInfo
import com.example.domain.repository.CardReaderCommunicateRepository
import com.example.domain.usecase.user.FetchConnectedUserInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow


@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
    private const val USER_INFORMATION = "userInformation"
    private const val SVC_TMS_URL = "https://svctms.mtouch.com"
    private const val SVC_API_URL = "https://svcapi.mtouch.com"

    @Provides
    @ViewModelScoped
    fun provideUserRepository(
        @ApplicationContext context: Context
    ): UserRepository = UserRepositoryImpl(
        apiService = RetrofitBuilder.getTmsAPIService(SVC_TMS_URL),
        userInformationDao = DatabaseHelper.getInstance(context).userInformationDao(),
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
    ): DeviceRepository = DeviceRepositoryImpl(DatabaseHelper.getInstance(context).deviceInfoDao())

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
    ): CardReaderCommunicateRepository {
        val emptyDeviceCommunicateManager = object : CardReaderCommunicateRepository {
            override val connectionStatus: MutableSharedFlow<CardReaderStatus.Connection>
                get() = MutableSharedFlow<CardReaderStatus.Connection>()
            override val dataStream: MutableSharedFlow<ByteArray>
                get() = MutableSharedFlow<ByteArray>()

            override fun connect(deviceInfo: String) {}
            override fun disConnect() {}
            override fun stopRetry() {}
            override fun sendData(byteArray: ByteArray) {}
        }

        return when(fetchConnectedDeviceInfo.getCurrentCardReaderData()) {
            is CardReaderData.Bluetooth -> BluetoothCardReaderRepositoryImpl(context)
            is CardReaderData.Usb -> UsbCardReaderRepositoryImpl(context)
            is CardReaderData.Init -> emptyDeviceCommunicateManager
        }
    }
}