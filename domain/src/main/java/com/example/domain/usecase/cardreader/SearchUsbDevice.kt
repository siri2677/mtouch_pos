package com.example.domain.usecase.cardreader

import com.example.domain.manager.cardreader.CardReaderSearchManager
import com.example.domain.model.cardreader.CardReaderData

class SearchUsbDevice(private val deviceScan: CardReaderSearchManager) {
    operator fun invoke(): List<CardReaderData> = deviceScan.run { scan(); deviceList.value }
}