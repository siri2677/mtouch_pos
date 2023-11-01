//package com.example.domain.usecase
//
//import android.util.Log
//import com.example.domain.vo.FallbackCode
//import com.example.domain.vo.KsnetParsingByte
//import com.example.domain.vo.ProcessAfterSerialCommunicate
//import com.example.domain.usecaseinterface.ResponseDeviceSerialCommunication
//import com.example.domain.util.socketClient.KsnetUtils
//import com.ksnet.interfaces.Approval
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.launch
//import java.nio.ByteBuffer
//import kotlin.experimental.xor
//
//
//class ResponseDeviceSerialCommunicationImpl: ResponseDeviceSerialCommunication {
//    private var receiveData = ByteArray(1024)
//    private var overWriteEmptyData = ByteArray(2048)
//    private var receiveDataLength = 0
//
//    private var isFirstPayment = false
//
//    private val hashReaderData = HashMap<String, ByteArray>()
//
//    override val afterProcess = MutableSharedFlow<ProcessAfterSerialCommunicate.ProcessValue>()
//    override val isCompletePayment = MutableSharedFlow<Boolean>()
//
//
//
//    fun makeRequestTelegram(
//        TransType: ByteArray?,
//        TelegramType: ByteArray?,
//        WorkType: ByteArray?,
//        TelegramNo: ByteArray,
//        PosEntry: ByteArray?,
//        AuthNum: ByteArray,
//        AuthDate: ByteArray,
//        trackId: ByteArray,
//        DPTID: ByteArray?,
//        SWModelNum: ByteArray?,
//        ReaderModelNum: ByteArray?,
//        PayType: ByteArray?,
//        TotalAmount: ByteArray?,
//        Amount: ByteArray?,
//        ServiceAmount: ByteArray?,
//        TaxAmount: ByteArray?,
//        FreeAmount: ByteArray?,
//        Filler: ByteArray?,
//        SignTrans: ByteArray?,
//        SignData: ByteArray?,
//        EncryptInfo: ByteArray?,
//        EMVData: ByteArray?,
//        TrackII: ByteArray?
//    ): ByteArray? {
//        var telegramBuffer = ByteBuffer.allocateDirect(4096)
//        telegramBuffer.put(0x02.toByte())
//        telegramBuffer.put(TransType) //거래구분
//        if (!String(WorkType!!).contains("09")) //업무구분 09(이통사 할인)시 worktype 09|이통사 형태로 들어옴..전문 reseved필드에 이통사 적용
//            telegramBuffer.put(WorkType)
//        else
//            telegramBuffer.put("09".toByteArray())
//        telegramBuffer.put(TelegramType)
//        telegramBuffer.put("N".toByteArray()) //거래형태 일반 N
//        telegramBuffer.put(DPTID) //단말기번호
//        for (i in 0..3) telegramBuffer.put(" ".toByteArray())
//        telegramBuffer.put(TelegramNo)
//        for (i in 0 until 12 - TelegramNo.size) telegramBuffer.put(" ".toByteArray()) //전문일련 번호(망취소 시 사용)
//        telegramBuffer.put(PosEntry) //POS Entry
//        for (i in 0..19) telegramBuffer.put(" ".toByteArray())
//        for (i in 0..19) telegramBuffer.put(" ".toByteArray())
//        // 암호화 여부
//
//        //Key-In 일 경우만
//        if ((String(TransType!!) == "HK" || String(TransType) == "PC") && String(PosEntry!!) == "K") telegramBuffer.put(
//            "9".toByteArray()
//        ) else telegramBuffer.put("1".toByteArray())
//        telegramBuffer.put(SWModelNum) // S/W 모델번호
//        telegramBuffer.put(ReaderModelNum) // 리더기 모델 정보
//        telegramBuffer.put(EncryptInfo) // 암호화정보
//        for (i in 0 until 40 - EncryptInfo!!.size) telegramBuffer.put(" ".toByteArray())
//        if (String(TransType) == "IC") // TRack II
//            for (i in 0..36) telegramBuffer.put(" ".toByteArray()) else if (String(TransType) == "MS" || String(
//                TransType
//            ) == "HK" || String(TransType) == "PC"
//        ) {
//            telegramBuffer.put(TrackII)
//            for (i in 0 until 37 - TrackII!!.size) telegramBuffer.put(" ".toByteArray())
//        }
//        telegramBuffer.put(0x1C.toByte()) // FS
//        telegramBuffer.put(PayType) // [신용]할부개월 , [현금] , [포인트]
//        telegramBuffer.put(TotalAmount) // 총금액
//        telegramBuffer.put(ServiceAmount) // 봉사료
//        telegramBuffer.put(TaxAmount) // 세금
//        telegramBuffer.put(Amount) // 공급금액
//        telegramBuffer.put(FreeAmount) // 면세금액
//        telegramBuffer.put("AA".toByteArray()) // Working Key Index
//        for (i in 0..15) telegramBuffer.put("0".toByteArray()) //비밀번호
//        //원거래 승인번호
//        if (String(TelegramType!!) == "0420" || String(TelegramType!!) == "0460") {
//            telegramBuffer.put(AuthNum)
//            for (i in 0 until 12 - AuthNum.size) telegramBuffer.put(" ".toByteArray()) //원거래승인번호
//            telegramBuffer.put(AuthDate)
//            for (i in 0 until 6 - AuthDate.size) telegramBuffer.put(" ".toByteArray()) //원거래승인일자
//        } else {
//            for (i in 0..11) telegramBuffer.put(" ".toByteArray()) //원거래승인번호
//            for (i in 0..5) telegramBuffer.put(" ".toByteArray()) //원거래승인일자
//        }
//        if (!String(WorkType).contains("09")) {               //할인
//            for (i in 0..14) telegramBuffer.put(" ".toByteArray()) //사용자정보 ~ 가맹점ID
//            if (trackId.size > 0 && trackId.size < 31) telegramBuffer.put(trackId)
//            for (i in 0 until 30 - trackId.size) telegramBuffer.put(" ".toByteArray()) //가맹점 DataField
//            for (i in 0..27) telegramBuffer.put(" ".toByteArray()) //Reserved ~ 신용카드종류
//        } else {
//            for (i in 0..44) telegramBuffer.put(" ".toByteArray()) //사용자정보
//
//
//            /* Reserved 필드 삽입 */
//            val strReserved = String(WorkType).split("|".toRegex()).dropLastWhile { it.isEmpty() }
//                .toTypedArray()[1]
//            telegramBuffer.put(strReserved.toByteArray())
//            for (i in 0 until 4 - strReserved.length) telegramBuffer.put(" ".toByteArray())
//            /* Reserved 필드 삽입 */for (i in 0..23)  //32 ~ 36 까지
//                telegramBuffer.put(" ".toByteArray()) //KSNET RESERVED ~ 신용카드 종류
//        }
//        telegramBuffer.put(Filler) //Filler
//        for (i in 0 until 30 - String(Filler!!).length) telegramBuffer.put(" ".toByteArray())
//        for (i in 0..59) telegramBuffer.put(" ".toByteArray()) //DCC환율조회
//
//        //IC 거래일 경우 EMV 데이터 추가
//        if (String(TransType) == "IC") telegramBuffer.put(EMVData)
//        if (String(SignTrans!!) == "N") telegramBuffer.put(SignTrans) //전자서명(무서명)
//        else {
//            telegramBuffer.put(SignTrans) // 전자서명(서명)
//            telegramBuffer.put("83".toByteArray()) //Working Key Index
//            for (i in 0..15) telegramBuffer.put(" ".toByteArray()) //제품 코드 및 버전
//            telegramBuffer.put(String.format("%04d", String(SignData!!).length).toByteArray())
//            telegramBuffer.put(SignData) // base64
//        }
//        telegramBuffer.put(0x03.toByte()) //ETX
//        telegramBuffer.put(0x0D.toByte()) //CR
//        val telegram = ByteArray(telegramBuffer.position())
//        telegramBuffer.rewind()
//        telegramBuffer[telegram]
//        val requestTelegram = ByteArray(telegram.size + 4)
//        val telegramLength = String.format("%04d", telegram.size)
//        System.arraycopy(telegramLength.toByteArray(), 0, requestTelegram, 0, 4)
//        System.arraycopy(telegram, 0, requestTelegram, 4, telegram.size)
//        val _overwrightData1 = ByteArray(telegram.size)
//        val _overwrightData2 = ByteArray(telegram.size)
//        for (i in telegram.indices) {
//            _overwrightData1[i] = 0x00.toByte()
//        }
//        for (i in telegram.indices) {
//            _overwrightData2[i] = 0xFF.toByte()
//        }
//        telegramBuffer.clear()
//        telegramBuffer.clear()
//        telegramBuffer.clear()
//        System.arraycopy(_overwrightData1, 0, telegram, 0, telegram.size)
//        System.arraycopy(_overwrightData2, 0, telegram, 0, telegram.size)
//        System.arraycopy(_overwrightData1, 0, telegram, 0, telegram.size)
//        return requestTelegram
//    }
//
//    fun threadAdmission() {
//        val bEncSign: ByteArray? = null
//        val mchtDataField = ByteArray(30)
//        for (i in 0..29) mchtDataField[i] = " ".toByteArray()[0]
//        val telegramNo = KsnetUtils().generateString(12)?.toByteArray()
//        val responseTelegram = ByteArray(2048)
//        val requestTelegram = makeRequestTelegram(
//            "IC".toByteArray(), //adminInfo.getTransType(),
//            "0420".toByteArray(), //adminInfo.getTelegramType(),
//            "01".toByteArray(), //adminInfo.getWorkType(),
//            telegramNo!!,
//            "S".toByteArray(), //adminInfo.getPosEntry(),
//            "30014624".toByteArray(), //bAdminNum,
//            "230811".toByteArray(), //bAdmindate,
//            mchtDataField,
//            "DPT0A24658".toByteArray(), //adminInfo.getDPTID(),
//            "######MTOUCH1101".toByteArray(),
//            "######KSR-051101".toByteArray(),
//            "00".toByteArray(), //adminInfo.getPayType(),
//            "000000001004".toByteArray(), //adminInfo.getTotalAmount(),
//            "000000000913".toByteArray(), //adminInfo.getAmount(),
//            "000000000000".toByteArray(), //adminInfo.getServicAmount(),
//            "000000000091".toByteArray(),
//            "000000000000".toByteArray(), //adminInfo.getFreeAmount(),
//            "".toByteArray(), //adminInfo.getFiller(),
//            "N".toByteArray(), //adminInfo.getSignTrans(),
//            bEncSign,
//            hashReaderData["EncryptInfo"],
//            hashReaderData["reqEMVData"],
//            hashReaderData["trackII"]
//        )
//
//        Thread {
//            val rtn: Int = Approval().request(
//                "210.181.28.137",
//                9562,
//                5,
//                requestTelegram,
//                responseTelegram,
//                16000
//            )
//
//            if (rtn < 0 && rtn != -102 && rtn != -103 && rtn != -104) {
//                Log.w("threadAdmmision", "threadAdmmision")
//            }
//            KsnetUtils().reqDataPrint(requestTelegram)
//            KsnetUtils().respGetHashData(responseTelegram)
//
//            if (rtn >= 0) {
//                CoroutineScope(Dispatchers.IO).launch {
//                    delay(2000)
//                    afterProcess.emit(ProcessAfterSerialCommunicate.ProcessValue(ProcessAfterSerialCommunicate.NoticeCompletePayment.name))
//                }
////                val req2thGenerate = EncMSRManager.make2ThGenerateReq(
////                    "000001004".toByteArray(), "00".toByteArray(),
////                    hashReaderData["tradeCnt"]!!, hashReaderData["reqEMVData"]!!
////                )
////                deviceSetting?.requestDeviceSerialCommunication(req2thGenerate)
//            }
//        }.start()
//    }
//
//    override fun init() {
//        isFirstPayment = false
//    }
//
//    private fun isFallbackMessage(message: String): Boolean {
//        for(fallbackMessage in FallbackCode.values()) {
//             if(fallbackMessage.code == message) return true
//        }
//        return false
//    }
////
////    private fun fallbackMessage(message: String): String {
////        for(fallbackMessage in FallbackMessage.values()) {
////            if(fallbackMessage.keyValue() == message) fallbackMessage.toString()
////        }
////        return ""
////    }
//
//    private fun receive(data: ByteArray): Int {
//        System.arraycopy(data, 0, receiveData, receiveDataLength, data.size)
//        receiveDataLength += data.size
//        if (receiveDataLength > 3) {
//            val bluetoothLength = KsnetUtils().byteToInt(receiveData[1]) * 256 + KsnetUtils().byteToInt(receiveData[2]) + 4
//            val bluetoothLRC = receiveData[bluetoothLength - 2].toInt() == 3 &&
//                    receiveData[bluetoothLength - 1] == checkLRC(KsnetUtils().byteToSubByte(receiveData, 0, bluetoothLength))
//            val usbLength = KsnetUtils().byte2Int(receiveData[1]) * 0xff + KsnetUtils().byte2Int(receiveData[2])
//            val usbLRC = receiveData != null && receiveData[usbLength + 2] == 0x03.toByte()
//
//            if (bluetoothLength >= 1024 || usbLength >= 1024) {
//                clearTempBuffer()
//                return 0
//            }
//            if (bluetoothLRC || usbLRC) {
//                var spn = "receive $receiveDataLength bytes\n"
//                if (data.isNotEmpty())
//                    spn += KsnetUtils().toHex(
//                        receiveData,
//                        receiveDataLength
//                    )
//                Log.w("responseData", spn)
//                return 1
//            }
//            return 0
//        }
//        return 0
//    }
//
//    private fun getCommandID(t_data: ByteArray?, resultDataLength: Int): Byte {
//        if (t_data == null || t_data.size < 6) {
//            return 1
//        }
//        if (t_data[0].toInt() != 2) {
//            return 2
//        }
//        return if (t_data[resultDataLength - 2].toInt() != 3) {
//            3
//        } else t_data[3]
//    }
//
//    private fun clearTempBuffer() {
//        receiveDataLength = 0
//        System.arraycopy(overWriteEmptyData, 0, receiveData, 0, 1024)
//    }
//
//    private fun checkLRC(bytes: ByteArray): Byte {
//        var lrc: Byte = 0
//        for (i in 1 until bytes.size - 1) {
//            lrc = (bytes[i] xor lrc)
//        }
//        return lrc
//    }
//
//
//}