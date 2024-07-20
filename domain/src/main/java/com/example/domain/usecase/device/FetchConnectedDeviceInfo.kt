package com.example.domain.usecase.device

import com.example.domain.repositoryInterface.DeviceRepository
import com.example.domain.model.device.DeviceInfo
import com.example.domain.model.user.UserDetailData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FetchConnectedDeviceInfo(
    private val deviceInfoAdapterFactoryGson: Gson,
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): DeviceInfo? = try {
        deviceInfoAdapterFactoryGson.fromJson(
            deviceRepository.getDeviceInformation(),
            object : TypeToken<DeviceInfo>() {}.type
        )
    } catch (e: NullPointerException) {
        null
    }
}
