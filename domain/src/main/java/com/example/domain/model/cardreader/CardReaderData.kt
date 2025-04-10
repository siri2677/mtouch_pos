package com.example.domain.model.cardreader



sealed interface CardReaderData {
    val deviceInformation: String
    val deviceName: String
    data class Init(
        override val deviceInformation: String = "",
        override val deviceName: String = ""
    ) : CardReaderData

    data class Bluetooth(
        override val deviceInformation: String,
        override val deviceName: String
    ) : CardReaderData

    data class Usb(
        override val deviceInformation: String,
        override val deviceName: String,
        val productName: String
    ) : CardReaderData
}