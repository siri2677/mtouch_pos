package com.example.domain.usecase

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.example.domain.enumclass.DeviceSharedPreferenceKey
import com.example.domain.enumclass.DeviceType
import com.example.domain.usecase.usb.UsbDeviceSearchUseCase
import com.google.gson.Gson



class DeviceSettingSharedPreferenceImpl(val context: Context) {
    fun getCurrentRegisteredDeviceType(): String {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        return sharedPreferences.getString(DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.toString(), DeviceSharedPreferenceKey.EMPTY_STRING.toString())!!
    }

    fun deleteCurrentRegisteredDeviceType() {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.toString(), DeviceSharedPreferenceKey.EMPTY_STRING.toString())
        editor.commit()
    }

    fun getBluetoothDeviceInformation(): String {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        return sharedPreferences.getString(DeviceSharedPreferenceKey.BLUETOOTH_DEVICE.toString(), DeviceSharedPreferenceKey.EMPTY_STRING.toString())!!
    }

    fun setBluetoothDeviceInformation(bluetoothDeviceAddress: String) {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(DeviceSharedPreferenceKey.BLUETOOTH_DEVICE.toString(), bluetoothDeviceAddress)
        editor.putString(DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.toString(),
            DeviceType.BLUETOOTH.name
        )
        editor.commit()
    }

    fun deleteBluetoothDeviceInformation() {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(DeviceSharedPreferenceKey.BLUETOOTH_DEVICE.toString(), DeviceSharedPreferenceKey.EMPTY_STRING.toString())
        editor.putString(DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.toString(),
            DeviceSharedPreferenceKey.EMPTY_STRING.toString()
        )
        editor.commit()
    }



    fun isKeepBluetoothConnection(): Boolean {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(DeviceSharedPreferenceKey.KEEP_BLUETOOTH_CONNECTION.toString(), false)
    }

    fun setKeepBluetoothConnection(isKeepBluetoothConnection: Boolean) {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(DeviceSharedPreferenceKey.KEEP_BLUETOOTH_CONNECTION.toString(), isKeepBluetoothConnection)
        editor.commit()
    }



    fun getUsbDeviceInformation(): String {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        return sharedPreferences.getString(DeviceSharedPreferenceKey.USB_DEVICE.toString(), DeviceSharedPreferenceKey.EMPTY_STRING.toString())!!
    }

    fun setUsbDeviceInformation(usbDeviceInformation: String) {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(DeviceSharedPreferenceKey.USB_DEVICE.toString(), usbDeviceInformation)
        editor.putString(DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.toString(),
            DeviceType.USB.name
        )
        editor.commit()
    }



    fun clearBluetoothDeviceInformation() {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(DeviceSharedPreferenceKey.BLUETOOTH_DEVICE.toString(),
            DeviceSharedPreferenceKey.EMPTY_STRING.toString()
        )
        editor.putString(DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.toString(),
            DeviceSharedPreferenceKey.EMPTY_STRING.toString()
        )
        editor.commit()
    }

    fun clearUsbDeviceInformation() {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(DeviceSharedPreferenceKey.USB_DEVICE.toString(),
            DeviceSharedPreferenceKey.EMPTY_STRING.toString()
        )
        editor.putString(DeviceSharedPreferenceKey.CURRENT_REGISTERED_DEVICE_TYPE.toString(),
            DeviceSharedPreferenceKey.EMPTY_STRING.toString()
        )
        editor.commit()
    }



    fun setBluetoothDisConnectButtonClick(isDisConnectButtonClick: Boolean) {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(DeviceSharedPreferenceKey.DISCONNECT_BLUTOOTH_DEVICE.toString(), isDisConnectButtonClick)
        editor.commit()
    }

    fun isBluetoothDisConnectButtonClick(): Boolean {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(DeviceSharedPreferenceKey.DISCONNECT_BLUTOOTH_DEVICE.toString(), false)
    }

    fun setUsbDisConnectButtonClick(isDisConnectButtonClick: Boolean) {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(DeviceSharedPreferenceKey.DISCONNECT_USB_DEVICE.toString(), isDisConnectButtonClick)
        editor.commit()
    }

    fun isUsbDisConnectButtonClick(): Boolean {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(DeviceSharedPreferenceKey.DISCONNECT_USB_DEVICE.toString(), false)
    }



    fun setBindingBluetoothService(isBindingBluetoothService: Boolean) {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(DeviceSharedPreferenceKey.BINDING_BLUETOOTH_SERVICE.toString(), isBindingBluetoothService)
        editor.commit()
    }

    fun isBindingBluetoothService(): Boolean {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(DeviceSharedPreferenceKey.BINDING_BLUETOOTH_SERVICE.toString(), false)
    }

    fun setBindingUsbService(isBindidngUsbService: Boolean) {
        val sharedPreferences = context.getSharedPreferences(DeviceSharedPreferenceKey.DEVICE_INFORMATION.toString(), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(DeviceSharedPreferenceKey.BINDING_USB_SERVICE.toString(), isBindidngUsbService)
        editor.commit()
    }



}