package com.example.domain.manager.cardreader

import com.example.domain.model.cardreader.CardReaderData
import kotlinx.coroutines.flow.MutableStateFlow

interface CardReaderSearchManager {
    val deviceList: MutableStateFlow<List<CardReaderData>>
    fun scan()
    fun cancel()
}