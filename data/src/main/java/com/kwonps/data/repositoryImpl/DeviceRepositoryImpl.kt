package com.kwonps.data.repositoryImpl

import com.kwonps.data.internal.dao.DeviceInfoDAO
import com.kwonps.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val deviceInfoDao: DeviceInfoDAO
): DeviceRepository {
    override fun getDeviceInfo() = deviceInfoDao.get().map { it.deviceInfo }

    override fun getCurrentRegisteredDeviceInfo() = deviceInfoDao.getCurrentRegisteredInfo().deviceInfo

    override fun setDeviceInfo(deviceInfo: String) {
        deviceInfoDao.updateDeviceInfo(deviceInfo)
    }
}