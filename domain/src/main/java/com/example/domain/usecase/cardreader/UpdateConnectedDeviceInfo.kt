package com.example.domain.usecase.cardreader

import com.example.domain.model.cardreader.CardReaderData
import com.example.domain.repository.DeviceRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UpdateConnectedDeviceInfo(
    private val deviceRepository: DeviceRepository,
    private val deviceInfoAdapterFactoryGson: Gson
) {
    operator fun invoke(cardReaderData: CardReaderData) {
        deviceRepository.setDeviceInfo(deviceInfoAdapterFactoryGson.toJson(cardReaderData, object : TypeToken<CardReaderData>() {}.type))
    }
}
