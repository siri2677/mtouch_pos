package com.example.cleanarchitech_text_0506.util

import com.example.domain.util.socketClient.KsnetUtils
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.experimental.and


class EncMSRManager {
    private val yymmddhhmmss = getTime()!!.substring(0, 12)
    private val stx: Byte = 0x02
    private val etx: Byte = 0x03.toByte()
    private val ksnet_dongle_info_req = 0xC0.toByte()
    private val KSNET_READER_SET_REQ = 0xC1.toByte()
    private val KSNET_CARDNO_REQ = 0xC2.toByte()
    private val KSNET_IC_2ND_REQ = 0xC3.toByte()
    private val KSNET_INTEGRITY_REQ = 0xC4.toByte()
    private val KSNET_FALLBACK_REQ = 0xC5.toByte()
    private val KSNET_IC_STATE_REQ = 0xC6.toByte()
    private val KSNET_KEY_SHARED_REQ = 0xC7.toByte()
    private val KSNET_Device_INFO_REQ = 0xCF.toByte()

    private var packet = ByteArray(1024)
    private var year: String? = getTime()?.substring(0, 2)

    private fun getTime(): String? {
        val time = System.currentTimeMillis()
        val dayTime = SimpleDateFormat("yyMMddhhmmss")
        return dayTime.format(Date(time))
    }

    private fun lrc(bytes: ByteArray, length: Int): Int {
        var checksum = 0
        //STX 1byte 뒤부터  ETX까지
        for (i in 1 until length) {
            checksum = checksum xor ((bytes[i] and 0xFF.toByte()).toInt())
        }
        return checksum
    }

    fun makeDongleInfo(): ByteArray {
        var packet = ByteArray(1024)
        var idx = 0
        packet[idx++] = stx //STX

        packet[idx++] = 0x00 //Length 2바이트

        packet[idx++] = 0x05
        packet[idx++] = ksnet_dongle_info_req // 'C0' Command

        packet[idx++] = year!![0].toByte()

        packet[idx++] = year!![1].toByte()
        packet[idx++] = '1'.toByte() //카드데이터형식 1:카드번호 마스킹 2: 논마스킹 3: 16자리 암호화 + 마스킹

        packet[idx++] = etx
        val bLRC: Byte = lrc(packet, idx).toByte()
        packet[idx++] = bLRC //LRC

        val txPacket = ByteArray(idx)
        System.arraycopy(packet, 0, txPacket, 0, idx)
        packet = ByteArray(1024) //패킷 초기화


        return txPacket
    }

    fun makeCardNumSendReq(totalAmount: ByteArray?, resTime: ByteArray?): ByteArray {
        var idx = 0
        val YYMMDDhhmmss = getTime()!!.substring(0, 12)

        packet[idx++] = stx //STX
        packet[idx++] = 0x00 //Length 2바이트
        packet[idx++] = 0x19
        packet[idx++] = KSNET_CARDNO_REQ // 'C2' Command


        System.arraycopy(YYMMDDhhmmss.toByteArray(), 0, packet, idx, 12)
        idx += 12
        System.arraycopy(totalAmount, 0, packet, idx, 9)
        idx += 9
        System.arraycopy(resTime, 0, packet, idx, 2)
        idx += 2

        packet[idx++] = etx
        val bLRC = lrc(packet, idx).toByte()
        packet[idx++] = bLRC //LRC


        val txPacket = ByteArray(idx)
        System.arraycopy(packet, 0, txPacket, 0, idx)

        packet = ByteArray(1024) //패킷 초기화

        return txPacket
    }

    fun makeFallBackCardReq(
        FallBcack_ErrCode: String,
        timeOut: String,
    ): ByteArray {
        var idx = 0
        packet[idx++] = stx //STX
        System.arraycopy(KsnetUtils().make2ByteLengh(25), 0, packet, idx, 2)
        val idx2 = idx + 2
        val idx3 = idx2 + 1
        packet[idx2] = KSNET_FALLBACK_REQ
        System.arraycopy(yymmddhhmmss.toByteArray(), 0, packet, idx3, 12)
        val idx4 = idx3 + 12
        System.arraycopy(
            String.format("%09d", *arrayOf<Any>(Integer.valueOf(FallBcack_ErrCode.toInt())))
                .toByteArray(), 0,
            packet, idx4, 9
        )
        val idx5 = idx4 + 9
        System.arraycopy(timeOut.toByteArray(), 0, packet, idx5, 2)
        val idx6 = idx5 + 2
        val idx7 = idx6 + 1
        packet[idx6] = 3
        val idx8 = idx7 + 1
        packet[idx7] = lrc(packet, idx7).toByte()
        val txPacket = ByteArray(idx8)
        System.arraycopy(packet, 0, txPacket, 0, idx8)
        packet = ByteArray(1024)
        return txPacket
    }

    fun getStrMoneytoTgAmount(Money: String?): ByteArray? {
        var Money = Money
        var TgAmount: ByteArray?
        return if (Money == null || Money.length == 0) {
            null
        } else {
            try {
                val longMoney = Money.trim { it <= ' ' }.replace(",", "").toLong()
                Money = String.format("%012d", longMoney)
                TgAmount = Money.toByteArray()
                TgAmount
            } catch (e: Exception) {
                null
            }
        }
    }


}