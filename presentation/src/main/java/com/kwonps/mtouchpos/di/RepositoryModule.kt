package com.kwonps.mtouchpos.di

import android.content.Context
import com.kwonps.data.common.dispatcher.DefaultDispatcherProvider
import com.kwonps.data.common.dispatcher.DispatcherProvider
import com.kwonps.data.common.interceptor.FlowCallDecorator
import com.kwonps.data.common.interceptor.RetryPolicy
import com.kwonps.data.common.logger.RepositoryLogger
import com.kwonps.data.internal.DatabaseHelper
import com.kwonps.data.mapper.DirectPaymentMapper
import com.kwonps.data.mapper.PaymentHistoryMapper
import com.kwonps.data.mapper.UserMapper
import com.kwonps.data.remote.RetrofitBuilder
import com.kwonps.data.repositoryImpl.BluetoothCardReaderRepositoryImpl
import com.kwonps.data.repositoryImpl.DeviceRepositoryImpl
import com.kwonps.data.repositoryImpl.DirectPaymentRepositoryImpl
import com.kwonps.data.repositoryImpl.OfflinePaymentRepositoryImpl
import com.kwonps.data.repositoryImpl.PaymentHistoryRepositoryImpl
import com.kwonps.data.repositoryImpl.UsbCardReaderRepositoryImpl
import com.kwonps.data.repositoryImpl.UserRepositoryImpl
import com.kwonps.data.source.payment.DirectPaymentRemoteDataSource
import com.kwonps.data.source.payment.PaymentHistoryRemoteDataSource
import com.kwonps.data.source.user.UserLocalDataSource
import com.kwonps.data.source.user.UserRemoteDataSource
import com.kwonps.domain.model.cardreader.CardReaderData
import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.repository.CardReaderCommunicateRepository
import com.kwonps.domain.repository.DeviceRepository
import com.kwonps.domain.repository.DirectPaymentRepository
import com.kwonps.domain.repository.OfflinePaymentRepository
import com.kwonps.domain.repository.PaymentHistoryRepository
import com.kwonps.domain.repository.UserRepository
import com.kwonps.domain.usecase.cardreader.FetchConnectedDeviceInfo
import com.kwonps.domain.usecase.user.FetchConnectedUserInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    private const val USER_INFORMATION = "userInformation"
    private const val SVC_TMS_URL = "https://svctms.mtouch.com"
    private const val SVC_API_URL = "https://svcapi.mtouch.com"

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideRepositoryLogger(): RepositoryLogger = RepositoryLogger()

    @Provides
    @Singleton
    fun provideRetryPolicy(): RetryPolicy = RetryPolicy()

    @Provides
    @Singleton
    fun provideFlowCallDecorator(
        repositoryLogger: RepositoryLogger,
        retryPolicy: RetryPolicy,
        dispatcherProvider: DispatcherProvider
    ): FlowCallDecorator = FlowCallDecorator(repositoryLogger, retryPolicy, dispatcherProvider)

    @Provides
    @Singleton
    fun providePaymentHistoryMapper(): PaymentHistoryMapper = PaymentHistoryMapper()

    @Provides
    @Singleton
    fun provideDirectPaymentMapper(): DirectPaymentMapper = DirectPaymentMapper()

    @Provides
    @Singleton
    fun provideUserMapper(): UserMapper = UserMapper()

    @Provides
    @Singleton
    fun provideUserRemoteDataSource(
        dispatcherProvider: DispatcherProvider
    ): UserRemoteDataSource = UserRemoteDataSource(
        apiService = RetrofitBuilder.getTmsAPIService(SVC_TMS_URL),
        dispatcherProvider = dispatcherProvider,
    )

    @Provides
    @Singleton
    fun provideUserLocalDataSource(
        @ApplicationContext context: Context
    ): UserLocalDataSource = UserLocalDataSource(
        dao = DatabaseHelper.getInstance(context).userInformationDao(),
        sharedPreferences = context.getSharedPreferences(USER_INFORMATION, Context.MODE_PRIVATE),
        preferenceKey = USER_INFORMATION
    )

    @Provides
    @Singleton
    fun provideUserRepository(
        userRemoteDataSource: UserRemoteDataSource,
        userLocalDataSource: UserLocalDataSource,
        userMapper: UserMapper,
        flowCallDecorator: FlowCallDecorator,
    ): UserRepository = UserRepositoryImpl(
        remoteDataSource = userRemoteDataSource,
        localDataSource = userLocalDataSource,
        mapper = userMapper,
        flowCallDecorator = flowCallDecorator,
    )

    @Provides
    @Singleton
    fun provideOfflinePaymentRepository(
        fetchConnectedUserInfo: FetchConnectedUserInfo
    ): OfflinePaymentRepository = OfflinePaymentRepositoryImpl(
        apiService = RetrofitBuilder.getTmsAPIService(SVC_TMS_URL),
        token = fetchConnectedUserInfo()?.key ?: "",
    )

    @Provides
    @Singleton
    fun provideDeviceRepository(
        @ApplicationContext context: Context
    ): DeviceRepository = DeviceRepositoryImpl(DatabaseHelper.getInstance(context).deviceInfoDao())

    @Provides
    @Singleton
    fun provideDirectPaymentRemoteDataSource(
        dispatcherProvider: DispatcherProvider,
        fetchConnectedUserInfo: FetchConnectedUserInfo
    ): DirectPaymentRemoteDataSource = DirectPaymentRemoteDataSource(
        apiService = RetrofitBuilder.getPayAPIService(SVC_API_URL),
        payKey = fetchConnectedUserInfo()?.payKey ?: "",
        dispatcherProvider = dispatcherProvider,
    )

    @Provides
    @Singleton
    fun provideDirectPaymentRepository(
        remoteDataSource: DirectPaymentRemoteDataSource,
        directPaymentMapper: DirectPaymentMapper,
        flowCallDecorator: FlowCallDecorator
    ): DirectPaymentRepository = DirectPaymentRepositoryImpl(
        remoteDataSource = remoteDataSource,
        mapper = directPaymentMapper,
        flowCallDecorator = flowCallDecorator,
    )

    @Provides
    @Singleton
    fun providePaymentHistoryRemoteDataSource(
        dispatcherProvider: DispatcherProvider,
        fetchConnectedUserInfo: FetchConnectedUserInfo,
        paymentHistoryMapper: PaymentHistoryMapper,
    ): PaymentHistoryRemoteDataSource = PaymentHistoryRemoteDataSource(
        apiService = RetrofitBuilder.getTmsAPIService(SVC_TMS_URL),
        token = fetchConnectedUserInfo()?.key ?: "",
        dispatcherProvider = dispatcherProvider,
        mapper = paymentHistoryMapper,
    )

    @Provides
    @Singleton
    fun providePaymentHistoryRepository(
        paymentHistoryRemoteDataSource: PaymentHistoryRemoteDataSource,
        paymentHistoryMapper: PaymentHistoryMapper,
        flowCallDecorator: FlowCallDecorator,
    ): PaymentHistoryRepository = PaymentHistoryRepositoryImpl(
        remoteDataSource = paymentHistoryRemoteDataSource,
        mapper = paymentHistoryMapper,
        flowCallDecorator = flowCallDecorator,
    )

    @Provides
    @Singleton
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

        return when (fetchConnectedDeviceInfo.getCurrentCardReaderData()) {
            is CardReaderData.Bluetooth -> BluetoothCardReaderRepositoryImpl(context)
            is CardReaderData.Usb -> UsbCardReaderRepositoryImpl(context)
            is CardReaderData.Init -> emptyDeviceCommunicateManager
        }
    }
}
