package com.kwonps.mtouchpos.fakes

import com.kwonps.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeDeviceRepository(initialInfo: String = "") : DeviceRepository {
    private val infoFlow = MutableStateFlow(initialInfo)

    override fun getDeviceInfo(): Flow<String> = infoFlow

    override fun getCurrentRegisteredDeviceInfo(): String = infoFlow.value

    override fun setDeviceInfo(deviceInformation: String) {
        infoFlow.value = deviceInformation
    }
}
