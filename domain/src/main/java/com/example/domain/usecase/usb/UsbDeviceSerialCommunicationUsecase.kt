package com.example.domain.usecase.usb

import com.example.domain.service.UsbConnectService

class UsbDeviceSerialCommunicationUsecase{
    fun requestDeviceSerialCommunication(useConnectService: UsbConnectService?, requestDataSerialCommunication: ByteArray){
        useConnectService!!.requestSerialCommunication(requestDataSerialCommunication)
    }
}