package com.kwonps.domain.usecase.cardreader

import com.kwonps.domain.adapter.JsonAdapter
import com.kwonps.domain.model.cardreader.CardReaderData
import com.kwonps.domain.repository.DeviceRepository

class UpdateConnectedDeviceInfo(
    private val jsonAdapter: JsonAdapter,
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(cardReaderData: CardReaderData) {
        deviceRepository.setDeviceInfo(
            jsonAdapter.toJson(cardReaderData, CardReaderData::class.java)
        )
    }
}
