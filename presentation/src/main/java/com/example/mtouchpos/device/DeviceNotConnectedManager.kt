package com.example.mtouchpos.device

import com.example.mtouchpos.device.deviceinterface.DeviceCommunicateManager

class DeviceNotConnectedManager: DeviceCommunicateManager {
    override fun bindingService() {}

    override fun unBindingService() {}

    override fun sendData(byteArray: ByteArray) {}
}