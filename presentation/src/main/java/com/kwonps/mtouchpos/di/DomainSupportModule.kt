package com.kwonps.mtouchpos.di

import com.google.gson.GsonBuilder
import com.kwonps.data.common.serialization.RuntimeTypeAdapterFactory
import com.kwonps.domain.adapter.JsonAdapter
import com.kwonps.domain.dispatcher.CoroutineDispatcherProvider
import com.kwonps.domain.model.cardreader.CardReaderData
import com.kwonps.mtouchpos.common.dispatcher.DefaultCoroutineDispatcherProvider
import com.kwonps.mtouchpos.common.serialization.GsonJsonAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainSupportModule {
    @Provides
    @Singleton
    fun provideCoroutineDispatcherProvider(): CoroutineDispatcherProvider =
        DefaultCoroutineDispatcherProvider()

    @Provides
    @Singleton
    fun provideJsonAdapter(): JsonAdapter {
        val gson = GsonBuilder().registerTypeAdapterFactory(
            RuntimeTypeAdapterFactory.of(CardReaderData::class.java, "type")
                .registerSubtype(CardReaderData.Bluetooth::class.java)
                .registerSubtype(CardReaderData.Usb::class.java)
        ).create()

        return GsonJsonAdapter(gson)
    }
}
