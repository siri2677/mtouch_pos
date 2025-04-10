package com.example.domain.usecase.cardreader

import com.example.domain.model.cardreader.CardReaderData
import com.example.domain.repository.DeviceRepository

class DeleteDeviceInfo(private val deviceRepository: DeviceRepository) {
    operator fun invoke() { deviceRepository.setDeviceInfo("") }
}