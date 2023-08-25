package com.example.cleanarchitech_text_0506.ditest.hiltmodule

import android.content.Context
import com.example.domain.usecase.bluetooth.BluetoothDeviceScanUseCase
import com.example.domain.usecase.usb.UsbDeviceSearchUseCase
import com.example.domain.usecaseinterface.BluetoothDeviceScanUsecaseImpl
import com.example.domain.usecaseinterface.UsbDeviceSearchUsecaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object DeviceScanViewModelModule {
    @Provides
    @ViewModelScoped
    fun provideBluetoothDeviceScanUsecaseImpl(
        @ApplicationContext context: Context
    ): BluetoothDeviceScanUsecaseImpl = BluetoothDeviceScanUseCase(context)

    @Provides
    @ViewModelScoped
    fun provideUsbDeviceSearchUsecaseImpl(
        @ApplicationContext context: Context
    ): UsbDeviceSearchUsecaseImpl = UsbDeviceSearchUseCase(context)
}