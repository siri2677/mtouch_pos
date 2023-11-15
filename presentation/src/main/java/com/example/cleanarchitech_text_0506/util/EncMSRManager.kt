package com.example.cleanarchitech_text_0506.util

import com.example.cleanarchitech_text_0506.vo.KsnetSocketCommunicationDTO
import java.nio.ByteBuffer
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
        System.arraycopy(make2ByteLengh(25), 0, packet, idx, 2)
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

    fun makeRequestTelegram(
        ksnetSocketCommunicationDTO: KsnetSocketCommunicationDTO
    ): ByteArray {
        var telegramBuffer = ByteBuffer.allocateDirect(4096)

        telegramBuffer.put(0x02.toByte())
        telegramBuffer.put(ksnetSocketCommunicationDTO.transType) //거래구분
        if (!String(ksnetSocketCommunicationDTO.workType!!).contains("09")) //업무구분 09(이통사 할인)시 worktype 09|이통사 형태로 들어옴..전문 reseved필드에 이통사 적용
            telegramBuffer.put(ksnetSocketCommunicationDTO.workType)
        else
            telegramBuffer.put("09".toByteArray())
        telegramBuffer.put(ksnetSocketCommunicationDTO.telegramType)
        telegramBuffer.put("N".toByteArray()) //거래형태 일반 N
        telegramBuffer.put(ksnetSocketCommunicationDTO.dptID) //단말기번호
        for (i in 0..3) telegramBuffer.put(" ".toByteArray())
        telegramBuffer.put(ksnetSocketCommunicationDTO.telegramNo)
        for (i in 0 until 12 - ksnetSocketCommunicationDTO.telegramNo.size) telegramBuffer.put(" ".toByteArray()) //전문일련 번호(망취소 시 사용)
        telegramBuffer.put(ksnetSocketCommunicationDTO.posEntry) //POS Entry
        for (i in 0..19) telegramBuffer.put(" ".toByteArray())
        for (i in 0..19) telegramBuffer.put(" ".toByteArray())
        // 암호화 여부

        //Key-In 일 경우만
        if ((String(ksnetSocketCommunicationDTO.transType!!) == "HK" || String(ksnetSocketCommunicationDTO.transType) == "PC") && String(ksnetSocketCommunicationDTO.posEntry!!) == "K") telegramBuffer.put(
            "9".toByteArray()
        ) else telegramBuffer.put("1".toByteArray())
        telegramBuffer.put(ksnetSocketCommunicationDTO.swModelNum) // S/W 모델번호
        telegramBuffer.put(ksnetSocketCommunicationDTO.readerModelNum) // 리더기 모델 정보
        telegramBuffer.put(ksnetSocketCommunicationDTO.encryptInfo) // 암호화정보
        for (i in 0 until 40 - ksnetSocketCommunicationDTO.encryptInfo!!.size) telegramBuffer.put(" ".toByteArray())
        if (String(ksnetSocketCommunicationDTO.transType) == "IC") // TRack II
            for (i in 0..36) telegramBuffer.put(" ".toByteArray()) else if (String(ksnetSocketCommunicationDTO.transType) == "MS" || String(
                ksnetSocketCommunicationDTO.transType
            ) == "HK" || String(ksnetSocketCommunicationDTO.transType) == "PC"
        ) {
            telegramBuffer.put(ksnetSocketCommunicationDTO.trackII)
            for (i in 0 until 37 - ksnetSocketCommunicationDTO.trackII!!.size) telegramBuffer.put(" ".toByteArray())
        }
        telegramBuffer.put(0x1C.toByte()) // FS
        telegramBuffer.put(ksnetSocketCommunicationDTO.payType) // [신용]할부개월 , [현금] , [포인트]
        telegramBuffer.put(ksnetSocketCommunicationDTO.totalAmount) // 총금액
        telegramBuffer.put(ksnetSocketCommunicationDTO.serviceAmount) // 봉사료
        telegramBuffer.put(ksnetSocketCommunicationDTO.taxAmount) // 세금
        telegramBuffer.put(ksnetSocketCommunicationDTO.amount) // 공급금액
        telegramBuffer.put(ksnetSocketCommunicationDTO.freeAmount) // 면세금액
        telegramBuffer.put("AA".toByteArray()) // Working Key Index
        for (i in 0..15) telegramBuffer.put("0".toByteArray()) //비밀번호
        //원거래 승인번호
        if (String(ksnetSocketCommunicationDTO.telegramType!!) == "0420" || String(ksnetSocketCommunicationDTO.telegramType!!) == "0460") {
            telegramBuffer.put(ksnetSocketCommunicationDTO.authNum)
            for (i in 0 until 12 - ksnetSocketCommunicationDTO.authNum.size) telegramBuffer.put(" ".toByteArray()) //원거래승인번호
            telegramBuffer.put(ksnetSocketCommunicationDTO.authDate)
            for (i in 0 until 6 - ksnetSocketCommunicationDTO.authDate.size) telegramBuffer.put(" ".toByteArray()) //원거래승인일자
        } else {
            for (i in 0..11) telegramBuffer.put(" ".toByteArray()) //원거래승인번호
            for (i in 0..5) telegramBuffer.put(" ".toByteArray()) //원거래승인일자
        }
        if (!String(ksnetSocketCommunicationDTO.workType).contains("09")) {               //할인
            for (i in 0..14) telegramBuffer.put(" ".toByteArray()) //사용자정보 ~ 가맹점ID
            if (ksnetSocketCommunicationDTO.trackId.size in 1..30) telegramBuffer.put(ksnetSocketCommunicationDTO.trackId)
            for (i in 0 until 30 - ksnetSocketCommunicationDTO.trackId.size) telegramBuffer.put(" ".toByteArray()) //가맹점 DataField
            for (i in 0..27) telegramBuffer.put(" ".toByteArray()) //Reserved ~ 신용카드종류
        } else {
            for (i in 0..44) telegramBuffer.put(" ".toByteArray()) //사용자정보


            /* Reserved 필드 삽입 */
            val strReserved = String(ksnetSocketCommunicationDTO.workType).split("|".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
            telegramBuffer.put(strReserved.toByteArray())
            for (i in 0 until 4 - strReserved.length) telegramBuffer.put(" ".toByteArray())
            /* Reserved 필드 삽입 */for (i in 0..23)  //32 ~ 36 까지
                telegramBuffer.put(" ".toByteArray()) //KSNET RESERVED ~ 신용카드 종류
        }
        telegramBuffer.put(ksnetSocketCommunicationDTO.filler) //Filler
        for (i in 0 until 30 - String(ksnetSocketCommunicationDTO.filler!!).length) telegramBuffer.put(" ".toByteArray())
        for (i in 0..59) telegramBuffer.put(" ".toByteArray()) //DCC환율조회

        //IC 거래일 경우 EMV 데이터 추가
        if (String(ksnetSocketCommunicationDTO.transType) == "IC") telegramBuffer.put(ksnetSocketCommunicationDTO.emvData)
        if (String(ksnetSocketCommunicationDTO.signTrans!!) == "N") telegramBuffer.put(ksnetSocketCommunicationDTO.signTrans) //전자서명(무서명)
        else {
            telegramBuffer.put(ksnetSocketCommunicationDTO.signTrans) // 전자서명(서명)
            telegramBuffer.put("83".toByteArray()) //Working Key Index
            for (i in 0..15) telegramBuffer.put(" ".toByteArray()) //제품 코드 및 버전
            telegramBuffer.put(String.format("%04d", String(ksnetSocketCommunicationDTO.signData!!).length).toByteArray())
            telegramBuffer.put(ksnetSocketCommunicationDTO.signData) // base64
        }
        telegramBuffer.put(0x03.toByte()) //ETX
        telegramBuffer.put(0x0D.toByte()) //CR
        val telegram = ByteArray(telegramBuffer.position())
        telegramBuffer.rewind()
        telegramBuffer[telegram]
        val requestTelegram = ByteArray(telegram.size + 4)
        val telegramLength = String.format("%04d", telegram.size)
        System.arraycopy(telegramLength.toByteArray(), 0, requestTelegram, 0, 4)
        System.arraycopy(telegram, 0, requestTelegram, 4, telegram.size)
        val _overwrightData1 = ByteArray(telegram.size)
        val _overwrightData2 = ByteArray(telegram.size)
        for (i in telegram.indices) {
            _overwrightData1[i] = 0x00.toByte()
        }
        for (i in telegram.indices) {
            _overwrightData2[i] = 0xFF.toByte()
        }
        telegramBuffer.clear()
        telegramBuffer.clear()
        telegramBuffer.clear()
        System.arraycopy(_overwrightData1, 0, telegram, 0, telegram.size)
        System.arraycopy(_overwrightData2, 0, telegram, 0, telegram.size)
        System.arraycopy(_overwrightData1, 0, telegram, 0, telegram.size)
        return requestTelegram
    }


    private fun make2ByteLengh(i: Int): ByteArray? {
        val bArr = ByteArray(2)
        bArr[1] = i.toByte()
        bArr[0] = (i ushr 8).toByte()
        return bArr
    }

}