package com.example.data.sharedpreference

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.domain.repositoryInterface.DeviceSettingSharedPreference
import com.example.domain.vo.DeviceType
import com.google.gson.Gson


class DeviceSettingSharedPreferenceImpl(val context: Context) : DeviceSettingSharedPreference {
    private val sharedPreferences = context.getSharedPreferences(
        DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(),
        Context.MODE_PRIVATE
    )

    data class DeviceInformation(
        val deviceType: DeviceType,
        val information: String
    )

    enum class DeviceSharedPreferenceKey {
        DEVICE_INFORMATION,
        EMPTY_STRING,
        BLUETOOTH_DEVICE,
        USB_DEVICE,
        DISCONNECT_BLUETOOTH_DEVICE,
        DISCONNECT_USB_DEVICE,
        CURRENT_REGISTERED_DEVICE_TYPE,
        KEEP_BLUETOOTH_CONNECTION
    }

    override fun getDeviceConnectSetting(): DeviceSettingSharedPreference.DeviceConnectSetting? {
        return Gson().fromJson(
            sharedPreferences.getString(
                DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.name,
                ""
            )!!,
            DeviceSettingSharedPreference.DeviceConnectSetting::class.java
        )
    }

    override fun setDeviceConnectSetting(deviceConnectSetting: DeviceSettingSharedPreference.DeviceConnectSetting) {
        sharedPreferences.edit().putString(
            DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.name,
            Gson().toJson(deviceConnectSetting)
        ).apply()
    }

    override fun saveDeviceConnectStatus(isConnected: Boolean) {
        sharedPreferences.edit().putString(
            DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.name,
            Gson().toJson(getDeviceConnectSetting()?.copy(isConnected = isConnected))
        ).apply()
    }

    override fun getCurrentRegisteredDeviceType(): DeviceType? {
        return Gson().fromJson(
            sharedPreferences.getString(
                DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.name,
                ""
            )!!,
            DeviceInformation::class.java
        ).deviceType
    }


    override fun getCurrentRegisteredDeviceInformation(): String? {
        return Gson().fromJson(
            sharedPreferences.getString(
                DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.name,
                ""
            )!!,
            DeviceInformation::class.java
        ).information
    }

    override fun setCurrentRegisteredDeviceType(deviceType: DeviceType, information: String) {
        sharedPreferences.edit().putString(
            DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.name,
            Gson().toJson(DeviceInformation(deviceType, information))
        ).apply()
    }

    override fun clearCurrentRegisteredDeviceType() {
        sharedPreferences.edit().putString(
            DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.name,
            ""
        ).commit()
    }

    override fun isKeepBluetoothConnection(): Boolean {
        val sharedPreferences = context.getSharedPreferences(
            DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(),
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getBoolean(
            DeviceSharedPreferenceKey.KEEP_BLUETOOTH_CONNECTION.toString(),
            false
        )
    }

    override fun setKeepBluetoothConnection(isKeepBluetoothConnection: Boolean) {
        val sharedPreferences = context.getSharedPreferences(
            DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(),
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putBoolean(
            DeviceSharedPreferenceKey.KEEP_BLUETOOTH_CONNECTION.toString(),
            isKeepBluetoothConnection
        )
        editor.commit()
    }

    fun setBluetoothDisConnectButtonClick(isDisConnectButtonClick: Boolean) {
        val sharedPreferences = context.getSharedPreferences(
            DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(),
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putBoolean(
            DeviceSharedPreferenceKey.DISCONNECT_BLUETOOTH_DEVICE.toString(),
            isDisConnectButtonClick
        )
        editor.commit()
    }

    fun isBluetoothDisConnectButtonClick(): Boolean {
        val sharedPreferences = context.getSharedPreferences(
            DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(),
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getBoolean(
            DeviceSharedPreferenceKey.DISCONNECT_BLUETOOTH_DEVICE.toString(),
            false
        )
    }

    fun setUsbDisConnectButtonClick(isDisConnectButtonClick: Boolean) {
        val sharedPreferences = context.getSharedPreferences(
            DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(),
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putBoolean(
            DeviceSharedPreferenceKey.DISCONNECT_USB_DEVICE.toString(),
            isDisConnectButtonClick
        )
        editor.commit()
    }

    fun isUsbDisConnectButtonClick(): Boolean {
        val sharedPreferences = context.getSharedPreferences(
            DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(),
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getBoolean(
            DeviceSharedPreferenceKey.DISCONNECT_USB_DEVICE.toString(),
            false
        )
    }
}