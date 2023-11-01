package com.example.cleanarchitech_text_0506.sealed

import com.example.cleanarchitech_text_0506.vo.KsnetSocketCommunicationDTO

sealed interface ProcessAfterSerialCommunicate {
    object RequestCardInsert: ProcessAfterSerialCommunicate
    data class RequestFallback(val cardType: String): ProcessAfterSerialCommunicate
    object RequestCardNumber: ProcessAfterSerialCommunicate
    data class RequestSocketCommunication(val byteArray: ByteArray): ProcessAfterSerialCommunicate
    object NoticeCompletePayment: ProcessAfterSerialCommunicate
}