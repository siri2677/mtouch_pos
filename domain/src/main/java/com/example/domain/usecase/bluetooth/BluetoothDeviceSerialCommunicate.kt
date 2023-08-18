package com.example.domain.usecase.bluetooth

import android.content.Context
import android.util.Log
import com.example.domain.service.BluetoothConnectService
import com.example.domain.usecase.DeviceSettingSharedPreferenceImpl
import javax.inject.Inject

class BluetoothDeviceSerialCommunicate(val context: Context) {
    fun requestSerialCommunicate(
        bluetoothConnectService: BluetoothConnectService,
        isConnectComplete: Boolean,
        requestDataSerialCommunication: ByteArray
    ) {
        if (!DeviceSettingSharedPreferenceImpl(context).isKeepBluetoothConnection() && !isConnectComplete) {
            Log.w("connectDevice", "connectDevice")
            bluetoothConnectService?.connectDevice()
        } else {
            Log.w("connectDeviceElse", "connectDeviceElse")
            bluetoothConnectService?.sendData(requestDataSerialCommunication)
        }
    }
}