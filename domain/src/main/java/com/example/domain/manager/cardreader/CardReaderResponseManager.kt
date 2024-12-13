package com.example.domain.manager.cardreader

import com.example.domain.model.cardreader.CardReaderStatus
import kotlinx.coroutines.flow.MutableSharedFlow

interface CardReaderResponseManager {
    val deviceConnectStatus: MutableSharedFlow<CardReaderStatus>
    val deviceSerialCommunicate: MutableSharedFlow<ByteArray>
}