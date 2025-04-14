package com.kwonps.data.service

import com.kwonps.domain.model.cardreader.CardReaderStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

object CardReaderResponse {
    val connectionStatus = MutableSharedFlow<CardReaderStatus.Connection>()
    val dataStream = MutableSharedFlow<ByteArray>()

    fun onResultCommunicate(byteArray: ByteArray) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStream.emit(byteArray)
        }
    }
}