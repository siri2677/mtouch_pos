package com.kwonps.domain.usecase.cardreader

import com.kwonps.domain.repository.DeviceRepository
import com.kwonps.domain.model.cardreader.CardReaderData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FetchConnectedDeviceInfo(
    private val deviceInfoAdapterFactoryGson: Gson,
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): StateFlow<CardReaderData> =
        deviceRepository.getDeviceInfo().map {
            deviceInfoAdapterFactoryGson.fromJson<CardReaderData>(
                it,
                object : TypeToken<CardReaderData>() {}.type
            ) ?: CardReaderData.Init()
        }.stateIn(CoroutineScope(Dispatchers.Main), SharingStarted.Lazily, CardReaderData.Init())

    fun getCurrentCardReaderData() = deviceInfoAdapterFactoryGson.fromJson<CardReaderData>(
        deviceRepository.getCurrentRegisteredDeviceInfo(),
        object : TypeToken<CardReaderData>() {}.type
    ) ?: CardReaderData.Init()
}
