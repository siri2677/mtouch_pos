package com.example.domain.usecaseinterface

import com.example.domain.enumclass.DeviceType
import com.example.domain.model.DeviceInformation

interface DeviceSettingSharedPreference {
    fun getCurrentRegisteredDeviceType(): DeviceInformation
    fun setCurrentRegisteredDeviceType(deviceType: DeviceType, information: String)
    fun clearCurrentRegisteredDeviceType()
}