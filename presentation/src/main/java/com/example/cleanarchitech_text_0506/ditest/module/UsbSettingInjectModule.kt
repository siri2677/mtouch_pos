package com.example.cleanarchitech_text_0506.di.module

import android.content.Context
import com.example.cleanarchitech_text_0506.ditest.ActivityScope
import com.example.domain.usecase.usb.UsbDeviceConnectUsecase
import com.example.domain.usecase.usb.UsbDeviceSearchUseCase
import com.example.domain.usecaseinterface.ResponseDeviceSerialCommunication
import com.example.domain.usecaseinterface.usb.UsbDeviceConnectUsecaseImpl
import com.example.domain.usecaseinterface.usb.UsbDeviceSearchUsecaseImpl
import dagger.Module
import dagger.Provides

@Module
class UsbSettingInjectModule {
    @Provides
    fun provideUsbDeviceSearchUsecaseImpl(context: Context): UsbDeviceSearchUsecaseImpl {
        return UsbDeviceSearchUseCase(context)
    }
//    @Provides
//    @ActivityScope
//    fun provideUsbDeviceConnectUserCaseImpl(context: Context): UsbDeviceConnectUsecaseImpl {
//        return UsbDeviceConnectUsecase(context)
//    }
//    @Provides
//    fun provideResponseDeviceSerialCommunicationImpl(): com.example.domain.usecaseinterface.ResponseDeviceSerialCommunication {
//        return ResponseDeviceSerialCommunication()
//    }
//    @Provides
//    @ActivityScope
//    fun provideUsbSerialCommunicationUseCaseImpl(context: Context, responseDeviceSerialCommunicationImpl: ResponseDeviceSerialCommunicationImpl): UsbDeviceSerialCommunicationUsecaseImpl {
//        return UsbDeviceSerialCommunicationUsecase(context, responseDeviceSerialCommunicationImpl)
//    }
}