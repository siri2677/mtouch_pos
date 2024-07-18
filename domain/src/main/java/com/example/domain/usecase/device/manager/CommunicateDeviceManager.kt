package com.example.domain.usecase.device.manager

interface CommunicateDeviceManager {
    fun bindingService()
    fun unBindingService()
    fun sendData(byteArray: ByteArray)
    fun isDeviceServiceInitialized(): Boolean
}