package com.example.cleanarchitech_text_0506.ditest.component

import com.example.cleanarchitech_text_0506.di.module.ViewModelModule
import com.example.cleanarchitech_text_0506.ditest.ActivityScope
import com.example.cleanarchitech_text_0506.view.ui.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class])
    abstract fun bindMainActivity(): MainActivity

}