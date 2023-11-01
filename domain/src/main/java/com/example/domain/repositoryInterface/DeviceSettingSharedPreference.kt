package com.example.domain.repositoryInterface


interface DeviceSettingSharedPreference {
    fun getCurrentRegisteredDeviceType(): String
    fun getCurrentRegisteredDeviceInformation(): String
    fun setCurrentRegisteredDeviceType(deviceType: String, information: String)
    fun clearCurrentRegisteredDeviceType()
    fun setKeepBluetoothConnection(boolean: Boolean)
    fun isKeepBluetoothConnection(): Boolean
}