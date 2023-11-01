package com.example.data.sharedpreference

import android.content.Context
import android.util.Log
import com.example.data.vo.DeviceInformation
import com.example.domain.repositoryInterface.DeviceSettingSharedPreference
import com.google.gson.Gson


class DeviceSettingSharedPreferenceImpl(val context: Context) : DeviceSettingSharedPreference {
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

    override fun getCurrentRegisteredDeviceType(): String {
        val sharedPreferences = context.getSharedPreferences(
            DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(),
            Context.MODE_PRIVATE
        )
        val userDataJson = sharedPreferences.getString(
            DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.toString(),
            DeviceSharedPreferenceKey.EMPTY_STRING.toString()
        )!!
        return Gson().fromJson(userDataJson, DeviceInformation::class.java).deviceType
    }

    override fun getCurrentRegisteredDeviceInformation(): String {
        val sharedPreferences = context.getSharedPreferences(
            DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(),
            Context.MODE_PRIVATE
        )
        val userDataJson = sharedPreferences.getString(
            DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.toString(),
            DeviceSharedPreferenceKey.EMPTY_STRING.toString()
        )!!
        return Gson().fromJson(userDataJson, DeviceInformation::class.java).information
    }

    override fun setCurrentRegisteredDeviceType(deviceType: String, information: String) {
        val sharedPreferences = context.getSharedPreferences(
            DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(),
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        val gson = Gson().toJson(DeviceInformation(deviceType, information))
        Log.w("setCurrent", gson)
        editor.putString(DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.toString(), gson)
        editor.apply()
    }

    override fun clearCurrentRegisteredDeviceType() {
        val sharedPreferences = context.getSharedPreferences(
            DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(),
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        val gson = Gson().toJson(DeviceInformation("", DeviceSharedPreferenceKey.EMPTY_STRING.name))
        editor.putString(DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.toString(), gson)
        editor.commit()
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