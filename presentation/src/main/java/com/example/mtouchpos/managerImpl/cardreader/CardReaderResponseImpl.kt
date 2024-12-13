package com.example.mtouchpos.managerImpl.cardreader

import com.example.domain.manager.cardreader.CardReaderResponseManager
import com.example.domain.model.cardreader.CardReaderStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

object CardReaderResponseImpl: CardReaderResponseManager {
    override val deviceConnectStatus = MutableSharedFlow<CardReaderStatus>()
    override val deviceSerialCommunicate = MutableSharedFlow<ByteArray>()
    fun onConnected() {
        CoroutineScope(Dispatchers.IO).launch {
            deviceConnectStatus.emit(CardReaderStatus.Connected)
        }
    }

    fun onRegistered() {
        CoroutineScope(Dispatchers.IO).launch {
            deviceConnectStatus.emit(CardReaderStatus.Registered)
        }
    }

    fun onDisConnected(retry: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            deviceConnectStatus.emit(CardReaderStatus.DisConnected(retry))
        }
    }

    fun onError(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            deviceConnectStatus.emit(CardReaderStatus.Error(message))
        }
    }

    fun onResultCommunicate(byteArray: ByteArray) {
        CoroutineScope(Dispatchers.IO).launch {
            deviceSerialCommunicate.emit(byteArray)
        }
    }
}