package com.example.domain.usecase.cardreader

import com.example.domain.manager.cardreader.CardReaderSearchManager
import com.example.domain.model.cardreader.CardReaderData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchBluetoothDevice(private val deviceScan: CardReaderSearchManager) {
    fun scan(): Flow<List<CardReaderData>> = flow {
        deviceScan.scan()
        deviceScan.deviceList.collect { emit(it) }
    }

    fun cancel() {
        deviceScan.cancel()
    }
}