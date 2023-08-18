package com.example.cleanarchitech_text_0506.di.component

import android.content.Context
import com.example.cleanarchitech_text_0506.di.module.BluetoothSettingInjectModule
import com.example.cleanarchitech_text_0506.di.module.LoginUseCaseInjectModule
import com.example.cleanarchitech_text_0506.di.module.UsbSettingInjectModule
import com.example.cleanarchitech_text_0506.di.module.ViewModelModule
import com.example.cleanarchitech_text_0506.ditest.ActivityScope
import com.example.cleanarchitech_text_0506.ditest.component.ActivityBindingModule
import com.example.cleanarchitech_text_0506.ditest.module.DeviceSerialCommunicateModule

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Component(modules = [
    AndroidSupportInjectionModule::class,
    ActivityBindingModule::class,
//    ActivityBindingModuleTest::class
])

interface ApplicationComponent : AndroidInjector<ApplicationComponent> {
    @Component.Factory
    interface Factory{
        fun create(@BindsInstance context: Context): ApplicationComponent
    }

    fun inject(myApp: MyApp)
}


