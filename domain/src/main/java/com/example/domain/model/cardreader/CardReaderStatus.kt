package com.example.domain.model.cardreader


sealed interface CardReaderStatus {
    sealed interface Connection: CardReaderStatus {
        data object Active: Connection
        data class Establishing(val attempts: Int): Connection
        data object Inactive: Connection
        data class Failure(val error: String): Connection
    }

    sealed interface Communication: CardReaderStatus {
        object Init: Communication
        data object InsertIC : Communication
        data object ReadingIC : Communication
        data class FallBack(
            val description: String,
            val code: String
        ): Communication
        data class result(
            val trackII: ByteArray,
            val readerModelNum: ByteArray,
            val encryptInfo: ByteArray,
            val reqEMVData: ByteArray,
            val cardNumber: String
        ): Communication
    }
}