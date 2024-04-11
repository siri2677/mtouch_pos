package com.example.mtouchpos.vo

import com.example.mtouchpos.R
import com.example.mtouchpos.dto.SerialInfo
import java.io.Serializable

sealed interface DeviceSerialCommunicate: Serializable {
    object Init: DeviceSerialCommunicate
    data class RequestSocketCommunication(val serialInfo: SerialInfo): DeviceSerialCommunicate
    open class SerialCommunicationMessage(val message: String, val painterInt: Int): DeviceSerialCommunicate {
        class IcCardInsertRequest: SerialCommunicationMessage("카드를 IC 슬롯에 넣어 주세요", R.drawable.pb2_4)
        class PaymentProgressing: SerialCommunicationMessage("결제 진행 중 입니다.\n 잠시만 기다려 주세요", R.drawable.pb2_2)
        class FallBackMessage: SerialCommunicationMessage("FallBack 거래발생. 마그네틱으로 카드를 읽혀주세요", R.drawable.pb2_3) {
            private lateinit var data: String
            fun getData(): String {
                return if(::data.isInitialized) data else ""
            }
            fun setData(data: String): SerialCommunicationMessage {
                this.data = data
                return this
            }
        }

//        fun fallbackDetail(detailMessage: String): String{
//            return message + detailMessage
//
    }
    data class ResultSerial(val data: ByteArray): DeviceSerialCommunicate
}