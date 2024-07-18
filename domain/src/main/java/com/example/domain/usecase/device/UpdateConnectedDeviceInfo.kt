package com.example.domain.usecase.device

import com.example.domain.repositoryInterface.DeviceRepository
import com.example.domain.model.device.DeviceInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UpdateConnectedDeviceInfo(
    private val deviceInfoAdapterFactoryGson: Gson,
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(deviceInfo: DeviceInfo) {
        deviceRepository.setDeviceInformation(
            deviceInfoAdapterFactoryGson.toJson(deviceInfo, object : TypeToken<DeviceInfo>() {}.type)
        )
    }
}
