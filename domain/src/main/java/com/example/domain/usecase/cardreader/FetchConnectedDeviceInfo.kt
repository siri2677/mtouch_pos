package com.example.domain.usecase.cardreader

import com.example.domain.repository.DeviceRepository
import com.example.domain.model.cardreader.CardReaderData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FetchConnectedDeviceInfo(
    private val deviceInfoAdapterFactoryGson: Gson,
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): CardReaderData? = try {
        deviceInfoAdapterFactoryGson.fromJson(
            deviceRepository.getDeviceInformation(),
            object : TypeToken<CardReaderData>() {}.type
        )
    } catch (e: NullPointerException) {
        null
    }
}
