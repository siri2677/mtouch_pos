package com.example.mtouchpos.device.deviceinterface

interface DeviceCommunicateManager {
    fun bindingService()
    fun unBindingService()
    fun sendData(byteArray: ByteArray)
}