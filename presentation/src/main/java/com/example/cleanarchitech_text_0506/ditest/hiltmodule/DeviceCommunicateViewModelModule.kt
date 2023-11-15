package com.example.cleanarchitech_text_0506.ditest.hiltmodule

import android.content.Context
import com.example.data.sharedpreference.DeviceSettingSharedPreferenceImpl
import com.example.domain.repositoryInterface.DeviceSettingSharedPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object DeviceCommunicateViewModelModule {
    @Provides
    @ViewModelScoped
    fun provideDeviceSettingSharedPreferenceImpl(
        @ApplicationContext context: Context
    ): DeviceSettingSharedPreference = DeviceSettingSharedPreferenceImpl(context)

//    @Provides
//    @ViewModelScoped
//    fun provideResponseDeviceSerialCommunicationImpl(): ResponseDeviceSerialCommunication = ResponseDeviceSerialCommunicationImpl()

//    @Provides
//    @Bluetooth
//    @ViewModelScoped
//    fun provideBluetoothDeviceSerialCommunicationUseCaseImpl(
//        @ApplicationContext context: Context,
//        responseDeviceSerialCommunication: ResponseDeviceSerialCommunication,
//    ): DeviceSetting = BluetoothDeviceSetting(context, responseDeviceSerialCommunication)
//
//    @Provides
//    @Usb
//    @ViewModelScoped
//    fun provideUsbSerialCommunicationUseCaseImpl(
//        @ApplicationContext context: Context,
//        responseDeviceSerialCommunication: ResponseDeviceSerialCommunication
//    ): DeviceSetting = UsbDeviceSetting(context, responseDeviceSerialCommunication)

}