package com.example.domain.repository

interface DeviceRepository {
    fun getDeviceInformation(): String?
    fun setDeviceInformation(deviceInformation: String)
}