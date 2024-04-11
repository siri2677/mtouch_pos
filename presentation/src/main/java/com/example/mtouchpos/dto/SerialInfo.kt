package com.example.mtouchpos.dto


data class SerialInfo(
    val trackII: ByteArray,
    val readerModelNum: ByteArray,
    val encryptInfo: ByteArray,
    val reqEMVData: ByteArray,
    val cardNumber: String
)