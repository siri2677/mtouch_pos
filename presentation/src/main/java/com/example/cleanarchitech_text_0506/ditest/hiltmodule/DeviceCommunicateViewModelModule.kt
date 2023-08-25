package com.example.cleanarchitech_text_0506.ditest.hiltmodule

import android.content.Context
import com.example.domain.usecase.DeviceSettingSharedPreferenceImpl
import com.example.domain.usecase.ResponseDeviceSerialCommunicationImpl
import com.example.domain.usecase.bluetooth.BluetoothDeviceSetting
import com.example.domain.usecase.usb.UsbDeviceSetting
import com.example.domain.usecaseinterface.DeviceSetting
import com.example.domain.usecaseinterface.DeviceSettingSharedPreference
import com.example.domain.usecaseinterface.ResponseDeviceSerialCommunication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Qualifier

@Module
@InstallIn(ViewModelComponent::class)
object DeviceCommunicateViewModelModule {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class Bluetooth
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class Usb

    @Provides
    @ViewModelScoped
    fun provideDeviceSettingSharedPreferenceImpl(
        @ApplicationContext context: Context
    ): DeviceSettingSharedPreference = DeviceSettingSharedPreferenceImpl(context)

    @Provides
    @ViewModelScoped
    fun provideResponseDeviceSerialCommunicationImpl(): ResponseDeviceSerialCommunication = ResponseDeviceSerialCommunicationImpl()

    @Provides
    @Bluetooth
    @ViewModelScoped
    fun provideBluetoothDeviceSerialCommunicationUseCaseImpl(
        @ApplicationContext context: Context,
        responseDeviceSerialCommunication: ResponseDeviceSerialCommunication,
    ): DeviceSetting = BluetoothDeviceSetting(context, responseDeviceSerialCommunication)

    @Provides
    @Usb
    @ViewModelScoped
    fun provideUsbSerialCommunicationUseCaseImpl(
        @ApplicationContext context: Context,
        responseDeviceSerialCommunication: ResponseDeviceSerialCommunication
    ): DeviceSetting = UsbDeviceSetting(context, responseDeviceSerialCommunication)

}