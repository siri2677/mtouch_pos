package com.kwonps.domain.usecase.cardreader

import com.kwonps.domain.adapter.JsonAdapter
import com.kwonps.domain.dispatcher.CoroutineDispatcherProvider
import com.kwonps.domain.model.cardreader.CardReaderData
import com.kwonps.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class FetchConnectedDeviceInfo(
    private val jsonAdapter: JsonAdapter,
    private val deviceRepository: DeviceRepository,
    private val dispatcherProvider: CoroutineDispatcherProvider
) {
    operator fun invoke(): Flow<CardReaderData> =
        deviceRepository.getDeviceInfo().map {
            jsonAdapter.fromJson(it, CardReaderData::class.java) ?: CardReaderData.Init()
        }.flowOn(dispatcherProvider.default)

    fun getCurrentCardReaderData() = jsonAdapter.fromJson(
        deviceRepository.getCurrentRegisteredDeviceInfo(),
        CardReaderData::class.java
    ) ?: CardReaderData.Init()
}
