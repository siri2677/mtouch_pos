package com.example.domain.usecase.device.manager

interface ConnectDeviceManager {
    fun connect(deviceInfo: String)
    fun disConnect()
}