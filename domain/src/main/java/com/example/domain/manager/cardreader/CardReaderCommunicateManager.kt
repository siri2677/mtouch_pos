package com.example.domain.manager.cardreader

import com.example.domain.model.cardreader.CardReaderStatus
import kotlinx.coroutines.flow.Flow

interface CardReaderCommunicateManager {
    fun bindingService()
    fun unBindingService()
    fun stopRetry(byteArray: ByteArray)
    fun connect(deviceInfo: String)
    fun sendData(byteArray: ByteArray)
//    fun isDeviceServiceInitialized(): Boolean
}