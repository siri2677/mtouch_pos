package com.mtouch.ksr02_03_04_v2.Domain.Model

import android.util.Log
import com.example.domain.usecase.KsnetUtils
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.experimental.and


object EncMSRManager {
    val YYMMDDhhmmss = getTime()!!.substring(0, 12)
    val STX: Byte = 0x02
    val ETX: Byte = 0x03.toByte()

    var packet = ByteArray(1024)

    var Year: String? = getTime()?.substring(0, 2)
    val KSNET_DONGLE_INFO_REQ = 0xC0.toByte()
    val KSNET_READER_SET_REQ = 0xC1.toByte()
    val KSNET_CARDNO_REQ = 0xC2.toByte()
    val KSNET_IC_2ND_REQ = 0xC3.toByte()
    val KSNET_INTEGRITY_REQ = 0xC4.toByte()
    val KSNET_FALLBACK_REQ = 0xC5.toByte()
    val KSNET_IC_STATE_REQ = 0xC6.toByte()
    val KSNET_KEY_SHARED_REQ = 0xC7.toByte()
    val KSNET_Device_INFO_REQ = 0xCF.toByte()

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
        packet[idx++] = STX //STX

        packet[idx++] = 0x00 //Length 2바이트

        packet[idx++] = 0x05
        packet[idx++] = KSNET_DONGLE_INFO_REQ // 'C0' Command

        packet[idx++] = Year!![0].toByte()

        packet[idx++] = Year!![1].toByte()
        packet[idx++] = '1'.toByte() //카드데이터형식 1:카드번호 마스킹 2: 논마스킹 3: 16자리 암호화 + 마스킹

        packet[idx++] = ETX
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

//        if(isKSR05) {
//            packet[idx++] = SOH;                   //SOH
//        }else{

//        if(isKSR05) {
//            packet[idx++] = SOH;                   //SOH
//        }else{
        packet[idx++] = STX //STX

//        }
        //        }
        packet[idx++] = 0x00 //Length 2바이트

        packet[idx++] = 0x19
        packet[idx++] = KSNET_CARDNO_REQ // 'C2' Command


        System.arraycopy(YYMMDDhhmmss.toByteArray(), 0, packet, idx, 12)
        idx += 12
        System.arraycopy(totalAmount, 0, packet, idx, 9)
        idx += 9
        System.arraycopy(resTime, 0, packet, idx, 2)
        idx += 2

        packet[idx++] = ETX
        val bLRC = lrc(packet, idx).toByte()
        packet[idx++] = bLRC //LRC


        val txPacket = ByteArray(idx)
        System.arraycopy(packet, 0, txPacket, 0, idx)

        packet = ByteArray(1024) //패킷 초기화

        return txPacket
    }

    fun makeRequestCardInOutStatus(): ByteArray {
        var idx = 0

//        if(isKSR05) {
//            packet[idx++] = SOH;                   //SOH
//        }else{
        packet[idx++] = STX //STX
        //        }
        packet[idx++] = 0x00 //Length 2바이트
        packet[idx++] = 0x05
        packet[idx++] = KSNET_IC_STATE_REQ // 'C6' Command
        packet[idx++] = " ".toByteArray()[0] //공백 3바이트
        packet[idx++] = " ".toByteArray()[0]
        packet[idx++] = " ".toByteArray()[0]
        packet[idx++] = ETX
        val bLRC = lrc(packet, idx).toByte()
        packet[idx++] = bLRC //LRC
        val txPacket = ByteArray(idx)
        System.arraycopy(packet, 0, txPacket, 0, idx)
        packet = ByteArray(1024) //패킷 초기화
        return txPacket
    }

    fun test(): ByteArray {
        var packet = ByteArray(29)
        var idx = 0
        packet[idx++] = 0x02
        packet[idx++] = 0x00
        packet[idx++] = 0x19
        packet[idx++] = 0xC2.toByte()
        packet[idx++] = 0x32
        packet[idx++] = 0x33
        packet[idx++] = 0x30
        packet[idx++] = 0x38
        packet[idx++] = 0x30
        packet[idx++] = 0x37
        packet[idx++] = 0x30
        packet[idx++] = 0x33
        packet[idx++] = 0x31
        packet[idx++] = 0x38
        packet[idx++] = 0x35
        packet[idx++] = 0x33
        packet[idx++] = 0x30
        packet[idx++] = 0x30
        packet[idx++] = 0x30
        packet[idx++] = 0x30
        packet[idx++] = 0x30
        packet[idx++] = 0x31
        packet[idx++] = 0x30
        packet[idx++] = 0x30
        packet[idx++] = 0x34
        packet[idx++] = 0x31
        packet[idx++] = 0x30
        packet[idx++] = 0x03
        packet[idx++] = 0xEE.toByte()
        return packet
    }

    fun test1(): ByteArray {
        var packet = ByteArray(29)
        var idx = 0
         packet[idx++] = 0x02
         packet[idx++] = 0x00
         packet[idx++] = 0x19
         packet[idx++] = 0xc5.toByte()
         packet[idx++] = 0x32
         packet[idx++] = 0x33
         packet[idx++] = 0x30
         packet[idx++] = 0x38
         packet[idx++] = 0x30
         packet[idx++] = 0x38
         packet[idx++] = 0x30
         packet[idx++] = 0x35
         packet[idx++] = 0x31
         packet[idx++] = 0x32
         packet[idx++] = 0x33
         packet[idx++] = 0x37
         packet[idx++] = 0x30
         packet[idx++] = 0x30
         packet[idx++] = 0x30
         packet[idx++] = 0x30
         packet[idx++] = 0x30
         packet[idx++] = 0x30
         packet[idx++] = 0x30
         packet[idx++] = 0x30
         packet[idx++] = 0x31
         packet[idx++] = 0x39
         packet[idx++] = 0x39
         packet[idx++] = 0x03
         packet[idx++] = 0xed.toByte()
        return packet
    }

    fun makeDeviceInfoReq(): ByteArray {
        var packet = ByteArray(1024)
        var idx = 0
        packet[idx++] = STX //STX

        packet[idx++] = 0x00 //Length 2바이트

        packet[idx++] = 0x04
        packet[idx++] = KSNET_Device_INFO_REQ // 'CF' Command

        packet[idx++] = Year!![0].toByte()

        packet[idx++] = Year!![1].toByte()

        packet[idx++] = ETX

        val bLRC: Byte = lrc(packet, idx).toByte()
        packet[idx++] = bLRC //LRC


        val txPacket = ByteArray(idx)
        System.arraycopy(packet, 0, txPacket, 0, idx)
        packet = ByteArray(1024) //패킷 초기화


        return txPacket
    }

//    fun chkFallBack(errCode: String?): String {
//        var errName = ""
//        when (errCode) {
//            FallbackMessage.FALLBACK_NO_ATR.keyValue() -> errName = "01"
//            FallbackMessage.FALLBACK_NO_APPL.keyValue() -> errName = "02"
//            FallbackMessage.FALLBACK_READ_FAIL.keyValue() -> errName = "03"
//            FallbackMessage.FALLBACK_NO_DATA.keyValue() -> errName = "04"
//            FallbackMessage.FALLBACK_CVM_FAIL.keyValue() -> errName = "05"
//            FallbackMessage.FALLBACK_BAD_CM.keyValue() -> errName = "06"
//            FallbackMessage.FALLBACK_BAD_OPER.keyValue() -> errName = "07"
////            FallbackMessage.NOFALLBACK_ApplicationBlock.keyValue() -> errName = "ApplicationBlock"
////            FallbackMessage.NOFALLBACK_ChipBlock.keyValue() -> errName = "ChipBlock"
////            FallbackMessage.NOFALLBACK_CardErr.keyValue() -> errName = "CardErr"
//        }
//        return errName
//    }


    fun makeRequestTelegram(
        TransType: ByteArray?,
        TelegramType: ByteArray?,
        WorkType: ByteArray?,
        TelegramNo: ByteArray,
        PosEntry: ByteArray?,
        AuthNum: ByteArray,
        AuthDate: ByteArray,
        trackId: ByteArray,
        DPTID: ByteArray?,
        SWModelNum: ByteArray?,
        ReaderModelNum: ByteArray?,
        PayType: ByteArray?,
        TotalAmount: ByteArray?,
        Amount: ByteArray?,
        ServiceAmount: ByteArray?,
        TaxAmount: ByteArray?,
        FreeAmount: ByteArray?,
        Filler: ByteArray?,
        SignTrans: ByteArray?,
        SignData: ByteArray?,
        EncryptInfo: ByteArray?,
        EMVData: ByteArray?,
        TrackII: ByteArray?
    ): ByteArray? {
        var telegramBuffer = ByteBuffer.allocateDirect(4096)
        telegramBuffer.put(0x02.toByte())
        telegramBuffer.put(TransType) //거래구분
        if (!String(WorkType!!).contains("09")) //업무구분 09(이통사 할인)시 worktype 09|이통사 형태로 들어옴..전문 reseved필드에 이통사 적용
            telegramBuffer.put(WorkType)
        else
            telegramBuffer.put("09".toByteArray())
        telegramBuffer.put(TelegramType)
        telegramBuffer.put("N".toByteArray()) //거래형태 일반 N
        telegramBuffer.put(DPTID) //단말기번호
        for (i in 0..3) telegramBuffer.put(" ".toByteArray())
        telegramBuffer.put(TelegramNo)
        for (i in 0 until 12 - TelegramNo.size) telegramBuffer.put(" ".toByteArray()) //전문일련 번호(망취소 시 사용)
        telegramBuffer.put(PosEntry) //POS Entry
        for (i in 0..19) telegramBuffer.put(" ".toByteArray())
        for (i in 0..19) telegramBuffer.put(" ".toByteArray())
        // 암호화 여부

        //Key-In 일 경우만
        if ((String(TransType!!) == "HK" || String(TransType) == "PC") && String(PosEntry!!) == "K") telegramBuffer.put(
            "9".toByteArray()
        ) else telegramBuffer.put("1".toByteArray())
        telegramBuffer.put(SWModelNum) // S/W 모델번호
        telegramBuffer.put(ReaderModelNum) // 리더기 모델 정보
        telegramBuffer.put(EncryptInfo) // 암호화정보
        for (i in 0 until 40 - EncryptInfo!!.size) telegramBuffer.put(" ".toByteArray())
        if (String(TransType) == "IC") // TRack II
            for (i in 0..36) telegramBuffer.put(" ".toByteArray()) else if (String(TransType) == "MS" || String(
                TransType
            ) == "HK" || String(TransType) == "PC"
        ) {
            telegramBuffer.put(TrackII)
            for (i in 0 until 37 - TrackII!!.size) telegramBuffer.put(" ".toByteArray())
        }
        telegramBuffer.put(0x1C.toByte()) // FS
        telegramBuffer.put(PayType) // [신용]할부개월 , [현금] , [포인트]
        telegramBuffer.put(TotalAmount) // 총금액
        telegramBuffer.put(ServiceAmount) // 봉사료
        telegramBuffer.put(TaxAmount) // 세금
        telegramBuffer.put(Amount) // 공급금액
        telegramBuffer.put(FreeAmount) // 면세금액
        telegramBuffer.put("AA".toByteArray()) // Working Key Index
        for (i in 0..15) telegramBuffer.put("0".toByteArray()) //비밀번호
        //원거래 승인번호
        if (String(TelegramType!!) == "0420" || String(TelegramType!!) == "0460") {
            telegramBuffer.put(AuthNum)
            for (i in 0 until 12 - AuthNum.size) telegramBuffer.put(" ".toByteArray()) //원거래승인번호
            telegramBuffer.put(AuthDate)
            for (i in 0 until 6 - AuthDate.size) telegramBuffer.put(" ".toByteArray()) //원거래승인일자
        } else {
            for (i in 0..11) telegramBuffer.put(" ".toByteArray()) //원거래승인번호
            for (i in 0..5) telegramBuffer.put(" ".toByteArray()) //원거래승인일자
        }
        if (!String(WorkType).contains("09")) {               //할인
            for (i in 0..14) telegramBuffer.put(" ".toByteArray()) //사용자정보 ~ 가맹점ID
            if (trackId.size > 0 && trackId.size < 31) telegramBuffer.put(trackId)
            for (i in 0 until 30 - trackId.size) telegramBuffer.put(" ".toByteArray()) //가맹점 DataField
            for (i in 0..27) telegramBuffer.put(" ".toByteArray()) //Reserved ~ 신용카드종류
        } else {
            for (i in 0..44) telegramBuffer.put(" ".toByteArray()) //사용자정보


            /* Reserved 필드 삽입 */
            val strReserved = String(WorkType).split("|".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
            telegramBuffer.put(strReserved.toByteArray())
            for (i in 0 until 4 - strReserved.length) telegramBuffer.put(" ".toByteArray())
            /* Reserved 필드 삽입 */for (i in 0..23)  //32 ~ 36 까지
                telegramBuffer.put(" ".toByteArray()) //KSNET RESERVED ~ 신용카드 종류
        }
        telegramBuffer.put(Filler) //Filler
        for (i in 0 until 30 - String(Filler!!).length) telegramBuffer.put(" ".toByteArray())
        for (i in 0..59) telegramBuffer.put(" ".toByteArray()) //DCC환율조회

        //IC 거래일 경우 EMV 데이터 추가
        if (String(TransType) == "IC") telegramBuffer.put(EMVData)
        if (String(SignTrans!!) == "N") telegramBuffer.put(SignTrans) //전자서명(무서명)
        else {
            telegramBuffer.put(SignTrans) // 전자서명(서명)
            telegramBuffer.put("83".toByteArray()) //Working Key Index
            for (i in 0..15) telegramBuffer.put(" ".toByteArray()) //제품 코드 및 버전
            telegramBuffer.put(String.format("%04d", String(SignData!!).length).toByteArray())
            telegramBuffer.put(SignData) // base64
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
        telegramBuffer = null
        System.arraycopy(_overwrightData1, 0, telegram, 0, telegram.size)
        System.arraycopy(_overwrightData2, 0, telegram, 0, telegram.size)
        System.arraycopy(_overwrightData1, 0, telegram, 0, telegram.size)
        return requestTelegram
    }

    fun make2ThGenerateReq(
        Amount: ByteArray,
        responseCode: ByteArray,
        tradeCode: ByteArray,
        resEMVData: ByteArray,
    ): ByteArray {
        var packet = ByteArray(1024)
        var idx = 0
        val YYMMDDhhmmss = getTime()!!.substring(0, 12)

//        if(isKSR05) {
//            packet[idx++] = SOH;                   //SOH
//        }else{
        packet[idx++] = STX //STX
        //        }
        val Length = 27 + resEMVData.size + 1 //Length : CommandID 부터 ETX 까지
        System.arraycopy(KsnetUtils().make2ByteLengh(Length), 0, packet, idx, 2)
        idx += 2
        packet[idx++] = KSNET_IC_2ND_REQ
        //거래일시
        System.arraycopy(YYMMDDhhmmss.toByteArray(), 0, packet, idx, 12)
        idx += 12
        //거래 총 금액
        System.arraycopy(Amount, 0, packet, idx, 9)
        idx += 9
        //승인응답 코드
        System.arraycopy(responseCode, 0, packet, idx, 2)
        idx += 2
        //IC 거래 코드
        System.arraycopy(tradeCode, 0, packet, idx, 3)
        idx += 3
        //Additional Response Data , ARPC, Issuer Script
        Log.w("resEMVData", resEMVData.toString())
        System.arraycopy(resEMVData, 0, packet, idx, resEMVData.size)
        idx += resEMVData.size
        packet[idx++] = ETX
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
        packet[idx++] = STX //STX
        System.arraycopy(KsnetUtils().make2ByteLengh(25), 0, packet, idx, 2)
        val idx2 = idx + 2
        val idx3 = idx2 + 1
        packet[idx2] = KSNET_FALLBACK_REQ
        System.arraycopy(YYMMDDhhmmss.toByteArray(), 0, packet, idx3, 12)
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


}