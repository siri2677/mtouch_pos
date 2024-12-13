package com.example.domain.model.cardreader


sealed interface CardReaderStatus {
    data object Connected: CardReaderStatus
    data object Registered: CardReaderStatus
    data class DisConnected(val retryCount: Int): CardReaderStatus
    data class Error(val message: String): CardReaderStatus
}