package com.example.cleanarchitech_text_0506.ditest.hiltmodule

import android.content.Context
import com.example.cleanarchitech_text_0506.annotation.Bluetooth
import com.example.cleanarchitech_text_0506.annotation.Usb
import com.example.cleanarchitech_text_0506.deviceinterface.DeviceScan
import com.example.cleanarchitech_text_0506.scandevice.BluetoothDeviceScan
import com.example.cleanarchitech_text_0506.scandevice.UsbDeviceScan
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Qualifier

@Module
@InstallIn(ViewModelComponent::class)
object DeviceScanViewModelModule {

    @Provides
    @ViewModelScoped
    @Usb
    fun provideUsbDeviceScanImpl(
        @ApplicationContext context: Context
    ): DeviceScan = UsbDeviceScan(context)

    @Provides
    @ViewModelScoped
    @Bluetooth
    fun provideBluetoothDeviceScanImpl(
        @ApplicationContext context: Context
    ): DeviceScan = BluetoothDeviceScan(context)
}