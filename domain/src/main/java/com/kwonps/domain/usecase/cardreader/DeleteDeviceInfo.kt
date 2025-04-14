package com.kwonps.domain.usecase.cardreader

import com.kwonps.domain.repository.DeviceRepository

class DeleteDeviceInfo(private val deviceRepository: DeviceRepository) {
    operator fun invoke() { deviceRepository.setDeviceInfo("") }
}