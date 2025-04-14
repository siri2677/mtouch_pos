package com.kwonps.mtouchpos.vo.type

sealed interface DeviceType {
    data class Bluetooth (val deviceInformation: String): DeviceType
    data class Usb (val deviceInformation: String): DeviceType
}