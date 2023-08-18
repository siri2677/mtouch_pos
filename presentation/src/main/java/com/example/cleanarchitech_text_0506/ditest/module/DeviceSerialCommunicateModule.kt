package com.example.cleanarchitech_text_0506.ditest.module

import android.content.Context
import com.example.cleanarchitech_text_0506.ditest.ActivityScope
import com.example.domain.usecase.RequestDeviceSerialCommunicateImpl
import com.example.domain.usecase.ResponseDeviceSerialCommunicationImpl
import com.example.domain.usecaseinterface.DeviceSetting
import com.example.domain.usecase.bluetooth.BluetoothDeviceSetting
import com.example.domain.usecase.usb.UsbDeviceSetting
import com.example.domain.usecaseinterface.RequestDeviceSerialCommunication
import com.example.domain.usecaseinterface.ResponseDeviceSerialCommunication
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class DeviceSerialCommunicateModule {
    @Provides
    @ActivityScope
    fun provideResponseDeviceSerialCommunicationImpl(): ResponseDeviceSerialCommunication {
        return ResponseDeviceSerialCommunicationImpl()
    }

    @Provides
    @ActivityScope
    @Named("Bluetooth")
    fun provideBluetoothDeviceSerialCommunicationUseCaseImpl(
        context: Context,
        responseDeviceSerialCommunication: ResponseDeviceSerialCommunication
    ): DeviceSetting {
        return BluetoothDeviceSetting(context, responseDeviceSerialCommunication)
    }

    @Provides
    @ActivityScope
    @Named("Usb")
    fun provideUsbSerialCommunicationUseCaseImpl(
        context: Context,
        responseDeviceSerialCommunication: ResponseDeviceSerialCommunication
    ): DeviceSetting {
        return UsbDeviceSetting(context, responseDeviceSerialCommunication)
    }

    @Provides
    @ActivityScope
    fun provideDeviceSerialCommunicationUseCaseImpl(
        context: Context,
        @Named("Bluetooth") bluetoothDeviceSerialCommunicationUseCaseImpl: DeviceSetting,
        @Named("Usb") usbDeviceSerialCommunicationUsecaseImpl: DeviceSetting
    ): RequestDeviceSerialCommunication {
        return RequestDeviceSerialCommunicateImpl(context, bluetoothDeviceSerialCommunicationUseCaseImpl, usbDeviceSerialCommunicationUsecaseImpl)
    }

}