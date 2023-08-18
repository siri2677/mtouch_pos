package com.example.domain.usecaseinterface.usb

import androidx.lifecycle.MutableLiveData
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event

interface UsbDeviceSerialCommunicationUsecaseImpl {
    fun requestDeviceSerialCommunication(byteArray: ByteArray)
    fun getResultDataSerialCommunication()
    fun unBindingService()
}