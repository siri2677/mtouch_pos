package com.example.domain.usecase.cardreader

import com.example.domain.manager.cardreader.CardReaderConnectManager
import com.example.domain.manager.cardreader.CardReaderResponseManager
import com.example.domain.model.cardreader.CardReaderStatus
import com.example.domain.model.cardreader.CardReaderData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ConnectCardReader(
    private val mutex: Mutex,
    private val deviceOperationCallback: CardReaderResponseManager
) {
    operator fun invoke(
        deviceInfo: CardReaderData,
        deviceConnectManager: CardReaderConnectManager
    ): Flow<CardReaderStatus> = flow {
        deviceConnectManager.connect(deviceInfo.deviceInformation)

        mutex.withLock {
            deviceOperationCallback.deviceConnectStatus.collect { emit(it) }
        }
    }
}