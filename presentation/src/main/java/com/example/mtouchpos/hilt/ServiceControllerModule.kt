package com.example.mtouchpos.hilt

import android.content.Context
import com.example.mtouchpos.device.deviceinterface.DeviceCommunicateManager
import com.example.domain.vo.DeviceType
import com.example.mtouchpos.device.bluetooth.BluetoothDeviceCommunicateManager
import com.example.mtouchpos.device.DeviceConnectManagerFactory
import com.example.mtouchpos.device.DeviceNotConnectedManager
import com.example.mtouchpos.device.usb.UsbDeviceCommunicateManager
import com.example.data.retrofit.repositoryimplement.RequestRemotePayRepositoryImpl
import com.example.data.retrofit.repositoryimplement.RequestRemoteTmsRepositoryImpl
import com.example.data.sharedpreference.DeviceSettingSharedPreferenceImpl
import com.example.domain.repositoryInterface.DeviceSettingSharedPreference
import com.example.domain.repositoryInterface.RequestRemoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
object ServiceControllerModule {
    @Provides
    @ViewModelScoped
    fun provideDeviceSettingSharedPreferenceImpl(
        @ApplicationContext context: Context
    ): DeviceSettingSharedPreference = DeviceSettingSharedPreferenceImpl(context)

    @Provides
    @ViewModelScoped
    fun provideConnectedDeviceController(
        @ApplicationContext context: Context,
        deviceSettingSharedPreference: DeviceSettingSharedPreference
    ): DeviceCommunicateManager {
        return when(deviceSettingSharedPreference.getDeviceConnectSetting()?.deviceType) {
            DeviceType.Bluetooth -> BluetoothDeviceCommunicateManager(context)
            DeviceType.Usb -> UsbDeviceCommunicateManager(context)
            null -> DeviceNotConnectedManager()
        }
    }

    @Provides
    @ViewModelScoped
    fun provideDeviceConnectManagerFactory(
        @ApplicationContext context: Context
    ): DeviceConnectManagerFactory = DeviceConnectManagerFactory(context)

    @Provides
    @ViewModelScoped
    @TmsRepository
    fun provideRequestRemoteTmsRepository(
        @ApplicationContext context: Context
    ): RequestRemoteRepository = RequestRemoteTmsRepositoryImpl(context)

    @Provides
    @ViewModelScoped
    @PayRepository
    fun provideRequestRemotePayRepository(
        @ApplicationContext context: Context
    ): RequestRemoteRepository = RequestRemotePayRepositoryImpl(context)
}