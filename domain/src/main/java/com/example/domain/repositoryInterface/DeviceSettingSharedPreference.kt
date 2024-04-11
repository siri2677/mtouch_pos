package com.example.domain.repositoryInterface

import com.example.domain.vo.DeviceType
import com.google.gson.Gson


interface DeviceSettingSharedPreference {
    data class DeviceConnectSetting(
        val deviceType: DeviceType,
        val deviceInformation: String,
        val isConnected: Boolean,
        val isKeepConnect: Boolean
    )

    fun getDeviceConnectSetting(): DeviceConnectSetting?
    fun setDeviceConnectSetting(deviceConnectSetting: DeviceConnectSetting)
    fun saveDeviceConnectStatus(isConnected: Boolean)

    fun getCurrentRegisteredDeviceType(): DeviceType?
    fun getCurrentRegisteredDeviceInformation(): String?
    fun setCurrentRegisteredDeviceType(deviceType: DeviceType, information: String)
    fun clearCurrentRegisteredDeviceType()
    fun setKeepBluetoothConnection(boolean: Boolean)
    fun isKeepBluetoothConnection(): Boolean
}