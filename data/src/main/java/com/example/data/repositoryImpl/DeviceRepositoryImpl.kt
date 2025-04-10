package com.example.data.repositoryImpl

import com.example.data.internal.dao.DeviceInfoDAO
import com.example.domain.model.cardreader.CardReaderData
import com.example.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
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