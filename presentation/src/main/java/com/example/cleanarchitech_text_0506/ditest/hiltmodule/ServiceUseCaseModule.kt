package com.example.cleanarchitech_text_0506.ditest.hiltmodule

import com.example.domain.usecaseinterface.ResponseDeviceSerialCommunication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceUseCaseModule {
//    @Provides
//    @ServiceScoped
//    fun provideResponseDeviceSerialCommunication(): ResponseDeviceSerialCommunication = ResponseDeviceSerialCommunicationImpl()
}
