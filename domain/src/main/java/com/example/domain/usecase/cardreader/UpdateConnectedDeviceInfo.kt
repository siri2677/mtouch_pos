package com.example.domain.usecase.cardreader

import com.example.domain.repository.DeviceRepository
import com.example.domain.model.cardreader.CardReaderData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UpdateConnectedDeviceInfo(
    private val deviceInfoAdapterFactoryGson: Gson,
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(deviceInfo: CardReaderData) {
        deviceRepository.setDeviceInformation(
            deviceInfoAdapterFactoryGson.toJson(deviceInfo, object : TypeToken<CardReaderData>() {}.type)
        )
    }
}
