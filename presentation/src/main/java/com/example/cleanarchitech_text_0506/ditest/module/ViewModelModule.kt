package com.example.cleanarchitech_text_0506.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cleanarchitech_text_0506.ditest.ActivityScope
import com.example.cleanarchitech_text_0506.ditest.module.DeviceSerialCommunicateModule
import com.example.cleanarchitech_text_0506.util.ViewModelFactory
import com.example.cleanarchitech_text_0506.util.ViewModelKey
import com.example.cleanarchitech_text_0506.viewmodel.BluetoothViewModel
import com.example.cleanarchitech_text_0506.viewmodel.DeviceSerialCommunicateViewModel
import com.example.cleanarchitech_text_0506.viewmodel.MainActivityViewModel
import com.example.domain.usecaseinterface.RequestDeviceSerialCommunication
import com.mtouch.ksr02_03_04_v2.Ui.UsbViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module(includes = [
    LoginUseCaseInjectModule::class,
    BluetoothSettingInjectModule::class,
    UsbSettingInjectModule::class,
    DeviceSerialCommunicateModule::class
])
abstract class ViewModelModule {
    @Binds
    abstract fun provideViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun provideTmsApiViewModel(tmsApiViewModel: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BluetoothViewModel::class)
    abstract fun provideBluetoothViewModel(bluetoothViewModel: BluetoothViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UsbViewModel::class)
    abstract fun provideMainViewModel(usbViewModel: UsbViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DeviceSerialCommunicateViewModel::class)
    abstract fun provideDeviceSerialCommunicateViewModel(deviceSerialCommunicationViewModel: DeviceSerialCommunicateViewModel): ViewModel

}