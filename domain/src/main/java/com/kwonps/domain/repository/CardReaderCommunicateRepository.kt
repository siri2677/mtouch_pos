package com.kwonps.domain.repository

import com.kwonps.domain.model.cardreader.CardReaderStatus
import kotlinx.coroutines.flow.MutableSharedFlow

interface CardReaderCommunicateRepository {
    val connectionStatus: MutableSharedFlow<CardReaderStatus.Connection>
    val dataStream: MutableSharedFlow<ByteArray>
    fun connect(deviceInfo: String)
    fun disConnect()
    fun stopRetry()
    fun sendData(byteArray: ByteArray, isPrint: Boolean = false)
}