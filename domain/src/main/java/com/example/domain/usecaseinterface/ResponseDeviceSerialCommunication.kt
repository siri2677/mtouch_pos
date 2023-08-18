package com.example.domain.usecaseinterface

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.domain.usecase.ResponseDeviceSerialCommunicationImpl
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event

interface ResponseDeviceSerialCommunication {
    val serialCommunicationMessage: MutableLiveData<Event<String?>?>
    val isCompletePayment: MutableLiveData<Event<Boolean?>?>
    fun setDeviceSetting(context: Context, deviceSetting: DeviceSetting): ResponseDeviceSerialCommunicationImpl
    fun receiveData(data: ByteArray)
    fun init()
}
