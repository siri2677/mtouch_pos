package com.example.domain.usecaseinterface.bluetooth

import android.bluetooth.BluetoothDevice

interface BluetoothDeviceRegister {
    fun bluetoothDeviceRegister(bluetoothDevice: BluetoothDevice)
    fun bluetoothDeviceUnRegister()
}