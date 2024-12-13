package com.example.domain.manager.cardreader

interface CardReaderConnectManager {
    fun connect(deviceInfo: String)
    fun disConnect()
}