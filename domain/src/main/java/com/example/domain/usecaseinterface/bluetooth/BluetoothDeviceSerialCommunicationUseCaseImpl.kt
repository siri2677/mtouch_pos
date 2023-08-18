package com.example.domain.usecaseinterface.bluetooth

import android.bluetooth.BluetoothDevice

interface BluetoothDeviceSerialCommunicationUseCaseImpl {
    fun requestDeviceSerialCommunication(byteArray: ByteArray)
    fun getResultDataSerialCommunication()
    fun unBindingService()
}