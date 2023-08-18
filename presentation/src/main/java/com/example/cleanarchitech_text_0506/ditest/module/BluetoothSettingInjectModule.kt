package com.example.cleanarchitech_text_0506.di.module

import android.content.Context
import com.example.cleanarchitech_text_0506.ditest.ActivityScope
import com.example.domain.usecaseinterface.bluetooth.BluetoothDeviceConnectUseCaseImpl
import com.example.domain.usecase.bluetooth.BluetoothDeviceScanUseCase
import com.example.domain.usecaseinterface.bluetooth.BluetoothDeviceScanUsecaseImpl
import com.example.domain.usecase.bluetooth.BluetoothDeviceConnectUseCase
import dagger.Module
import dagger.Provides

@Module
class BluetoothSettingInjectModule {
    @Provides
    fun provideBluetoothDeviceScanUsecaseImpl(context: Context): BluetoothDeviceScanUsecaseImpl {
        return BluetoothDeviceScanUseCase(context)
    }
//    @Provides
//    @ActivityScope
//    fun provideBluetoothDeviceSerialCommunicationUseCaseImpl(context: Context): BluetoothDeviceSerialCommunicationUseCaseImpl {
//        return BluetoothDeviceSerialCommunicationUseCase(context)
//    }

}