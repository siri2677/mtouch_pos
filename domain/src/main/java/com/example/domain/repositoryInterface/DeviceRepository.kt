package com.example.domain.repositoryInterface

interface DeviceRepository {
    fun getDeviceInformation(): String?
    fun setDeviceInformation(deviceInformation: String)
}