package com.example.domain.usecaseinterface

import kotlinx.coroutines.flow.MutableSharedFlow

interface ResponseDeviceSerialCommunication {
//    val afterProcess: MutableSharedFlow<ProcessAfterSerialCommunicate.ProcessValue>
    val isCompletePayment: MutableSharedFlow<Boolean>
    suspend fun receiveData(data: ByteArray)
    fun init()
}
