package com.example.cleanarchitech_text_0506.ditest.hiltmodule

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent

@Module
@InstallIn(ServiceComponent::class)
object ServiceUseCaseModule {
//    @Provides
//    @ServiceScoped
//    fun provideResponseDeviceSerialCommunication(): ResponseDeviceSerialCommunication = ResponseDeviceSerialCommunicationImpl()
}
