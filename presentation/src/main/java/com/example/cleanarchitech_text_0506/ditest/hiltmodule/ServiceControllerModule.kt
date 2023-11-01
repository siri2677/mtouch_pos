package com.example.cleanarchitech_text_0506.ditest.hiltmodule

import android.content.Context
import com.example.cleanarchitech_text_0506.annotation.Bluetooth
import com.example.cleanarchitech_text_0506.annotation.Usb
import com.example.cleanarchitech_text_0506.deviceinterface.DeviceServiceController
import com.example.cleanarchitech_text_0506.servicecontroller.BluetoothServiceController
import com.example.cleanarchitech_text_0506.servicecontroller.UsbServiceController
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
    @Bluetooth
    fun provideBluetoothServiceController(
        @ApplicationContext context: Context
    ): DeviceServiceController = BluetoothServiceController(context)
    @Provides
    @ViewModelScoped
    @Usb
    fun provideUsbServiceController(
        @ApplicationContext context: Context
    ): DeviceServiceController = UsbServiceController(context)
}