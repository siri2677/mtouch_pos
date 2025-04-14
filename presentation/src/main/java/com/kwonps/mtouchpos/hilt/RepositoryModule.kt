package com.kwonps.mtouchpos.hilt

import android.content.Context
import com.kwonps.data.internal.DatabaseHelper
import com.kwonps.data.remote.RetrofitBuilder
import com.kwonps.data.repositoryImpl.BluetoothCardReaderRepositoryImpl
import com.kwonps.data.repositoryImpl.DeviceRepositoryImpl
import com.kwonps.data.repositoryImpl.DirectPaymentRepositoryImpl
import com.kwonps.data.repositoryImpl.OfflinePaymentRepositoryImpl
import com.kwonps.data.repositoryImpl.PaymentHistoryRepositoryImpl
import com.kwonps.data.repositoryImpl.UsbCardReaderRepositoryImpl
import com.kwonps.data.repositoryImpl.UserRepositoryImpl
import com.kwonps.domain.model.cardreader.CardReaderData
import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.repository.DeviceRepository
import com.kwonps.domain.repository.DirectPaymentRepository
import com.kwonps.domain.repository.OfflinePaymentRepository
import com.kwonps.domain.repository.PaymentHistoryRepository
import com.kwonps.domain.repository.UserRepository
import com.kwonps.domain.usecase.cardreader.FetchConnectedDeviceInfo
import com.kwonps.domain.repository.CardReaderCommunicateRepository
import com.kwonps.domain.usecase.user.FetchConnectedUserInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableSharedFlow


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
            override fun sendData(byteArray: ByteArray, isPrint: Boolean) {}
        }

        return when(fetchConnectedDeviceInfo.getCurrentCardReaderData()) {
            is CardReaderData.Bluetooth -> BluetoothCardReaderRepositoryImpl(context)
            is CardReaderData.Usb -> UsbCardReaderRepositoryImpl(context)
            is CardReaderData.Init -> emptyDeviceCommunicateManager
        }
    }
}