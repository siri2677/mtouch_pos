package com.example.cleanarchitech_text_0506.util

import android.util.Log
import com.example.cleanarchitech_text_0506.enum.SerialCommunicationMessage
import com.example.cleanarchitech_text_0506.sealed.DeviceConnectSharedFlow
import com.example.cleanarchitech_text_0506.sealed.ProcessAfterSerialCommunicate
import com.example.cleanarchitech_text_0506.vo.KsnetSocketCommunicationDTO
import com.example.domain.dto.request.tms.RequestInsertPaymentDataDTO
import com.example.domain.util.socketClient.KsnetUtils
import com.example.domain.vo.FallbackCode
import com.example.domain.vo.KsnetParsingByte

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import kotlin.experimental.xor

class ResponseSerialCommunicationFormat {
    private var receiveData = ByteArray(1024)
    private var overWriteEmptyData = ByteArray(2048)
    private var receiveDataLength = 0

    private var isFirstPayment = false

    private val hashReaderData = HashMap<String, ByteArray>()

//    val afterProcess = MutableSharedFlow<ProcessAfterSerialCommunicate>()
    val isCompletePayment = MutableSharedFlow<Boolean>()

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

//    fun threadAdmission() {
//        val bEncSign: ByteArray? = null
//        val mchtDataField = ByteArray(30)
//        for (i in 0..29) mchtDataField[i] = " ".toByteArray()[0]
//        val telegramNo = KsnetUtils().generateString(12)?.toByteArray()
//        val responseTelegram = ByteArray(2048)
//        val requestTelegram = makeRequestTelegram(
//            "IC".toByteArray(),
//            "0420".toByteArray(),
//            "01".toByteArray(),
//            telegramNo!!,
//            "S".toByteArray(),
//            "30014624".toByteArray(),
//            "230811".toByteArray(),
//            mchtDataField,
//            "DPT0A24658".toByteArray(),
//            "######MTOUCH1101".toByteArray(),
//            "######KSR-051101".toByteArray(),
//            "00".toByteArray(),
//            "000000001004".toByteArray(),
//            "000000000913".toByteArray(),
//            "000000000000".toByteArray(),
//            "000000000091".toByteArray(),
//            "000000000000".toByteArray(),
//            "".toByteArray(),
//            "N".toByteArray(),
//            bEncSign,
//            hashReaderData["EncryptInfo"],
//            hashReaderData["reqEMVData"],
//            hashReaderData["trackII"]
//        )

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
//                    afterProcess.emit(ProcessAfterSerialCommunicate.NoticeCompletePayment)
////                    afterProcess.emit(ProcessAfterSerialCommunicate.ProcessValue(ProcessAfterSerialCommunicate.NoticeCompletePayment.name))
//                }
//                val req2thGenerate = EncMSRManager.make2ThGenerateReq(
//                    "000001004".toByteArray(), "00".toByteArray(),
//                    hashReaderData["tradeCnt"]!!, hashReaderData["reqEMVData"]!!
//                )
//                deviceSetting?.requestDeviceSerialCommunication(req2thGenerate)
//            }
//        }.start()
//    }

    suspend fun receiveData(
        data: ByteArray,
        ksnetSocketCommunicationDTO: KsnetSocketCommunicationDTO,
        deviceConnectSharedFlow: MutableSharedFlow<DeviceConnectSharedFlow>
    ) {
        if (receive(data) == 1) {
            val resultData = ByteArray(1024)
            val resultDataLength = receiveDataLength
            System.arraycopy(receiveData, 0, resultData, 0, receiveData.size)
            clearTempBuffer()
            val cmd: Byte = getCommandID(resultData, resultDataLength)
            when (cmd) {
                0xD0.toByte() -> {
                    isFirstPayment = true
                    deviceConnectSharedFlow.emit(
                        DeviceConnectSharedFlow.SerialCommunicationMessageFlow(SerialCommunicationMessage.IcCardInsertRequest.message)
                    )
//                    afterProcess.emit(ProcessAfterSerialCommunicate.RequestCardInsert)


//                    afterProcess.emit(
//                        ProcessAfterSerialCommunicate.ProcessValue(
//                            ProcessAfterSerialCommunicate.RequestCardInsert.name))
                }

                0xD2.toByte() -> {
                    var tradeCnt: String
                    var encInfoLen: Int
                    var encCardNum16Len: Int //암호화된 카드번호 필드 길이
                    var noEncCardNumLen: Int //암호화하지 않은 카드번호 길이
                    var reqEMVDataLen: Int //EMV 요청 Data 길이
                    var trackIILen: Int //Track2 Data 길이
                    var readerModelNum: ByteArray
                    var cardBin: ByteArray
                    var emvData: ByteArray
                    val cardType = KsnetUtils().byteToString(resultData, KsnetParsingByte.IDX_DATA.keyValue(), 2)!!

                    isFirstPayment = false
                    if (!isFallbackMessage(cardType)) {   // 정상적인 응답인 경우
                        if (cardType == "IC" || cardType == "MS") {
                            tradeCnt = KsnetUtils().byteToString(
                                resultData,
                                KsnetParsingByte.IDX_DATA.keyValue() + "IC".length,
                                3
                            )
                            readerModelNum = KsnetUtils().byteToString(
                                resultData,
                                KsnetParsingByte.IDX_DATA.keyValue() + 5,
                                16
                            ).toByteArray()
                            encInfoLen = KsnetUtils().byteToString(
                                resultData,
                                KsnetParsingByte.IDX_DATA.keyValue() + 5 + 16,
                                1
                            ).toByteArray()[0] + 1
//                            if (String(adminInfo.getPlayType()) == "D") { //데몬일 경우 식별정보 출력
//                                addText("Deamon") // 데몬
//                            }
                            //암호화 정보생성
                            val reqEncryptInfo = ByteArray(encInfoLen)
                            System.arraycopy(
                                resultData,
                                KsnetParsingByte.IDX_DATA.keyValue() + 5 + 16,
                                reqEncryptInfo,
                                0,
                                reqEncryptInfo.size
                            )
//                            hashReaderData["EncryptInfo"] = reqEncryptInfo

                            //EMV DATA
                            encCardNum16Len = KsnetUtils().byteToString(
                                resultData,
                                KsnetParsingByte.IDX_DATA.keyValue() + 5 + 16 + encInfoLen,
                                1
                            ).toByteArray()[0] + 1
                            if (encCardNum16Len === 0) encCardNum16Len = 1
                            noEncCardNumLen = KsnetUtils().byteToString(
                                resultData,
                                KsnetParsingByte.IDX_DATA.keyValue() + 5 + 16 + encInfoLen + encCardNum16Len,
                                1
                            ).toByteArray()[0] + 1 //암호화 하지 않은 카드번호 길이
                            val cardDataIdx: Int = KsnetParsingByte.IDX_DATA.keyValue() + "IC".length + tradeCnt.length + readerModelNum.size + encInfoLen //카드데이터 필드 인덱스
//                            hashReaderData["tradeCnt"] = tradeCnt.toByteArray()
//                            hashReaderData["readerModelNum"] = readerModelNum
//                            hashReaderData["transType"] = "IC".toByteArray()
                            if (cardType == "IC") {
//                                if (String(adminInfo.getReceiptNo()) == "") {//현금영수증 카드 승인일 경우  park 이경우에도 넘겨 주어야 합니다
////                                        clearTempBuffer();
////                                        Toast.makeText(PayResultActivity.this, "현금영수증 카드를 사용해주세요", Toast.LENGTH_LONG).show();
////                                        finish();
////                                        return;
//                                    _HashReaderData.put("transType", "HK".toByteArray())
//                                    addText("현금영수증카드 IC승인이 진행중입니다.")
//                                } else {
//                                    _HashReaderData.put("transType", "IC".toByteArray())
//                                    addText("IC승인이 진행중입니다.")
//                                }
//                                if (!isCardCancel) {
//                                    if (String(adminInfo.getReceiptNo()) == "") {
//                                        SweetDialog(getString(R.string.reader_iccard_cash))
//                                    } else {
//                                        SweetDialog(getString(R.string.reader_iccard_approce))
//                                    }
//                                }
//                                responseobj.setProcessingCd("1003")
                                reqEMVDataLen = resultDataLength - (cardDataIdx + encCardNum16Len + noEncCardNumLen) - 2 //EMV DATA 길이
//                                cardBin = ByteArray(noEncCardNumLen - 2)
//                                System.arraycopy(
//                                    resultData,
//                                    cardDataIdx + encCardNum16Len + 2,
//                                    cardBin,
//                                    0,
//                                    cardBin.size
//                                )
//                                hashReaderData["Cardbin"] = cardBin
//                                addText("ICardbin : " + Cardbin.length)
//                                addText("ICardbin : " + String(Cardbin))
//                                LOG.w("=========================================================")
//                                AndroidUtils.printHex(Cardbin)
//                                LOG.w("=========================================================")

                                //수신 EMV데이터로 EMV 요청전문 생성
                                emvData = ByteArray(reqEMVDataLen)
                                System.arraycopy(
                                    resultData,
                                    cardDataIdx + encCardNum16Len + noEncCardNumLen,
                                    emvData,
                                    0,
                                    emvData.size
                                )
                                val reqEMVData = ByteArray(emvData.size + 4)
                                val telegramLength = String.format("%04d", emvData.size)
                                System.arraycopy(
                                    telegramLength.toByteArray(),
                                    0,
                                    reqEMVData,
                                    0,
                                    4
                                )
                                System.arraycopy(emvData, 0, reqEMVData, 4, emvData.size)
//                                hashReaderData["reqEMVData"] = reqEMVData
//                                hashReaderData["trackII"] = " ".toByteArray()

                                val bEncSign: ByteArray? = null
                                val mchtDataField = ByteArray(30)
                                for (i in 0..29) mchtDataField[i] = " ".toByteArray()[0]
                                val telegramNo = KsnetUtils().generateString(12).toByteArray()

                                val requestTelegram = KsnetSocketCommunicationDTO(
                                    transType = "IC".toByteArray(),
                                    telegramType = "0200".toByteArray(),
                                    workType = "01".toByteArray(),
                                    telegramNo = telegramNo,
                                    posEntry = "S".toByteArray(),
                                    authNum = "30014624".toByteArray(),
                                    authDate = "230811".toByteArray(),
                                    trackId = mchtDataField,
                                    dptID = "DPT0A24658".toByteArray(),
                                    swModelNum = "######MTOUCH1101".toByteArray(),
                                    readerModelNum = "######KSR-051101".toByteArray(),
                                    payType = "00".toByteArray(),
                                    totalAmount = "000000001004".toByteArray(),
                                    amount = "000000000913".toByteArray(),
                                    freeAmount = "000000000000".toByteArray(),
                                    serviceAmount = "000000000091".toByteArray(),
                                    taxAmount = "000000000000".toByteArray(),
                                    filler = "".toByteArray(),
                                    signTrans = "N".toByteArray(),
                                    signData = bEncSign,
                                    encryptInfo = reqEncryptInfo,
                                    emvData = reqEMVData,
                                    trackII = " ".toByteArray(),
                                    installment = "00"
                                )

//                                ksnetSocketCommunicationDTO.encryptInfo = reqEncryptInfo
//                                ksnetSocketCommunicationDTO.emvData = reqEMVData
//                                ksnetSocketCommunicationDTO.readerModelNum = readerModelNum

                                deviceConnectSharedFlow.emit(DeviceConnectSharedFlow.RequestSocketCommunication(makeRequestTelegram(requestTelegram)))
//                                afterProcess.emit(ProcessAfterSerialCommunicate.RequestSocketCommunication(makeRequestTelegram(ksnetSocketCommunicationDTO)))

//                                afterProcess.emit(
//                                    ProcessAfterSerialCommunicate.ProcessValue(ProcessAfterSerialCommunicate.RequestSocketCommunication.name, cardType)
//                                )
//                                clearTempBuffer()
//                                threadAdmission()

//                                //50000만원 이하 무서명 거래
//                                if (String(adminInfo.getTotalAmount()).toLong() >= 50000) {
//                                    val i = Intent(
//                                        this@PayResultActivity,
//                                        PayCreditSign::class.java
//                                    ) // card view
//                                    i.putExtra("amount", String(adminInfo.getTotalAmount()))
//                                    startActivityForResult(i, ACTIVITY_MENU_GET_SIGN)
//                                } else {
//                                    ThreadAdmission(_HashReaderData)
//                                }
                            } // IC CARD
                        }
                    } else {
//                        serialCommunicationInsetCardStatus = SerialCommunicationInsertCardStatus.D5
//                        val data: ByteArray = EncMSRManager().makeFallBackCardReq(cardType, "99")
//                        serialCommunicate?.sendData(data)
//                        SerialCommunicationMessage.FallBackMessage.message += fallbackMessage(cardType)
//                        afterProcess.emit(
//                            ProcessAfterSerialCommunicate.ProcessValue(
//                                ProcessAfterSerialCommunicate.RequestFallback.name, cardType))
                        var serialCommunicationMessageFlow = DeviceConnectSharedFlow.SerialCommunicationMessageFlow(SerialCommunicationMessage.FallBackMessage.message)
                        serialCommunicationMessageFlow.setData(cardType)
                        deviceConnectSharedFlow.emit(serialCommunicationMessageFlow)
//                        afterProcess.emit(ProcessAfterSerialCommunicate.RequestFallback(cardType))
                    }
                    //IC우선거래가 아닌 일반 MS 거래시 거래진행
//                            if (ICGubu.equals("MS")) {
////                                    LOG.w("tag data","recvData: "+new String(resultData));
////                                    LOG.w("tag data","recvData: "+new String(KsnetUtils.KsnetUtils().byteToString(resultData, IDX_DATA + 5 + 16 + encInfoLen + encCardNum16Len, noEncCardNumLen-1)));
////                                    LOG.w("tag data","receiptNo: "+new String(adminInfo.getReceiptNo()) +" length: "+adminInfo.getReceiptNo().length+" value: "+adminInfo.getReceiptNo());
//                                if (!isFallback &&
//                                    (adminInfo.getReceiptNo().length !== 0
//                                            && KsnetUtils.KsnetUtils().byteToString(
//                                        resultData,
//                                        IDX_DATA + 5 + 16 + encInfoLen + encCardNum16Len + noEncCardNumLen - 1,
//                                        1
//                                    ).equals("2") ||
//                                            KsnetUtils.KsnetUtils().byteToString(
//                                                resultData,
//                                                IDX_DATA + 5 + 16 + encInfoLen + encCardNum16Len + noEncCardNumLen - 1,
//                                                1
//                                            ).equals("6"))
//                                ) {
//                                    SweetDialog(getString(R.string.reader_card_read_ic))
//                                    isFirstCardNunResq = true
//                                    isICFirstAct = true
//                                    clearTerminalBuffer()
//                                } else {
//                                    SweetDialog("MS카드 승인이 진행중입니다")
//                                    _HashReaderData.put("transType", "MS".toByteArray())
//                                    if (String(adminInfo.getReceiptNo()) == "X") {
//                                        SweetDialog("MS카드 승인이 진행중입니다")
//                                        _HashReaderData.put("transType", "MS".toByteArray())
//                                    } else {
//                                        SweetDialog("현금영수증 승인이 진행중입니다")
//                                        _HashReaderData.put("transType", "HK".toByteArray())
//                                    }
//                                }
//                                if (!isICFirstAct) {
//                                    trackIILen = KsnetUtils.KsnetUtils().byteToString(
//                                        resultData,
//                                        IDX_DATA + 5 + 16 + encInfoLen + encCardNum16Len + noEncCardNumLen,
//                                        1
//                                    ).toByteArray().get(0) as Int + 1
//                                    trackII = ByteArray(trackIILen + 1)
//                                    System.arraycopy(
//                                        resultData,
//                                        IDX_DATA + 5 + 16 + encInfoLen + encCardNum16Len + noEncCardNumLen,
//                                        trackII,
//                                        0,
//                                        trackII.length
//                                    )
//                                    val arrTrackII: Array<String> =
//                                        String(trackII).split("=".toRegex())
//                                            .dropLastWhile { it.isEmpty() }
//                                            .toTypedArray()
//                                    _HashReaderData.put("trackII", trackII)
//                                    if (String(_HashReaderData.get("transType")) == "HK" || String(
//                                            _HashReaderData.get("transType")
//                                        ) == "PC"
//                                    ) {
//                                        Cardbin = ByteArray(noEncCardNumLen - 3)
//                                        //Cardbin = Utils.byteToSubByte(resultData, cardDataIdx + encCardNum16Len + 2, noEncCardNumLen - 3);
//                                        System.arraycopy(
//                                            resultData,
//                                            cardDataIdx + encCardNum16Len + 2,
//                                            Cardbin,
//                                            0,
//                                            Cardbin.length
//                                        )
//                                    } else {
//                                        if (noEncCardNumLen < 4) {
//                                            SweetDialog(getString(R.string.reader_card_read_fail2))
//                                            clearTerminalBuffer()
//                                            responseobj.setResultCd("-1")
//                                            responseobj.setResultMsg(getString(R.string.reader_card_read_fail2))
//                                            returnActivity(RESULT_CANCELED)
//                                            return
//                                        }
//                                        Cardbin = ByteArray(noEncCardNumLen - 4)
//                                        System.arraycopy(
//                                            resultData,
//                                            cardDataIdx + encCardNum16Len + 2,
//                                            Cardbin,
//                                            0,
//                                            Cardbin.length
//                                        )
//                                    }
//                                    try {
//                                        if (String(Cardbin).contains("=")) {
//                                            Cardbin = String(Cardbin).split("=".toRegex())
//                                                .dropLastWhile { it.isEmpty() }
//                                                .toTypedArray().get(0).toByteArray()
//                                        }
//                                        _HashReaderData.put("Cardbin", Cardbin)
//                                    } catch (e: java.lang.Exception) {
//                                        _HashReaderData.put("Cardbin", Cardbin)
//                                    }
//                                    _HashReaderData.put("reqEMVData", reqEMVData)
//                                    addText("=========================================================")
//                                    addText(
//                                        "ICardbin" + Cardbin.length + " : " + String(Cardbin) + " : " + AndroidUtils.printHex(
//                                            Cardbin
//                                        )
//                                    )
//                                    addText("=========================================================")
//
//
//                                    //50000만원 이하 무서명 거래
//                                    isICFirstAct = false
//                                    if (String(adminInfo.getTotalAmount()).toLong() >= 50000) {
//                                        val i = Intent(
//                                            this@PayResultActivity,
//                                            PayCreditSign::class.java
//                                        ) // card view
//                                        //	Intent i = new Intent(activity, PayCreditSignCyrexPay.class);  // card view
//                                        i.putExtra("amount", String(adminInfo.getTotalAmount()))
//                                        startActivityForResult(i, ACTIVITY_MENU_GET_SIGN)
//                                    } else {
//                                        ThreadAdmission(_HashReaderData)
//                                    }
//                                }
//                            }
//                        }
//                    }

                }

                0xD3.toByte() -> {
                    Log.w("0xD3", KsnetUtils().byteToString(resultData, 4, 2))
//                    if(KsnetUtils().byteToString(resultData, 4, 2) == "00") {
//                        serialCommunicationResponse.value =
//                            SerialCommunicationResponse(SerialCommunicationInsertCardStatus.D3.name)
////                        clearTempBuffer()
//                        Log.w("0xD3", "0xD3")
//                    }
                }

                0xD5.toByte() -> {
                    val tradeCnt: String
                    val encInfoLen: ByteArray
                    val requestEncInfoLen: Int

                    val encCardNum16Len: Int //암호화된 카드번호 필드 길이
                    val noEncCardNumLen: Int //암호화하지 않은 카드번호 길이

                    val emvData: ByteArray
                    val reqEMVDataLen: Int //EMV 요청 Data 길이
                    val reqEMVData: ByteArray

                    val readerModelNum: ByteArray
                    var cardBin: ByteArray

                    if(KsnetUtils().byteToString(resultData, KsnetParsingByte.IDX_DATA.keyValue(), 2) == ("FB")){
                        tradeCnt = KsnetUtils().byteToString(resultData, "FB".length + 4, 3)
                        readerModelNum = KsnetUtils().byteToString(resultData, 9, 16).toByteArray()

                        requestEncInfoLen = KsnetUtils().byteToString(resultData, 25, 1).toByteArray()[0] + 1
                        encInfoLen = ByteArray(requestEncInfoLen)
                        System.arraycopy(resultData, 25, encInfoLen, 0, encInfoLen.size)
                        hashReaderData["EncryptInfo"] = encInfoLen

                        encCardNum16Len =
                            KsnetUtils().byteToString(resultData, requestEncInfoLen + 25, 1).toByteArray()[0] + 1
                        noEncCardNumLen =
                            KsnetUtils().byteToString(resultData, requestEncInfoLen + 25 + encCardNum16Len, 1)
                                .toByteArray()[0] + 1
                        val cardDataIdx2: Int =
                            "IC".length + 4 + tradeCnt.length + readerModelNum.size + requestEncInfoLen
                        cardBin = ByteArray(noEncCardNumLen - 2)
                        System.arraycopy(
                            resultData,
                            encCardNum16Len + cardDataIdx2 + 2,
                            cardBin,
                            0,
                            cardBin.size
                        )

                        try {
                            if (String(cardBin).contains("=")) {
                                cardBin = String(cardBin).split("=".toRegex())
                                    .dropLastWhile { it.isEmpty() }.toTypedArray().get(0)
                                    .toByteArray()
                            }
                            hashReaderData["Cardbin"] = cardBin
                        } catch (e: java.lang.Exception) {
                            hashReaderData["Cardbin"] = cardBin
                        }

//                        addText("FB MSCardbin : " + Cardbin.length)
//                        addText("FB MSCardbin : " + String(Cardbin))
//                        LOG.w("=========================================================")
//                        AndroidUtils.printHex(Cardbin)
//                        LOG.w("=========================================================")
//
//                        SweetDialog("MSR 승인을 진행중입니다")
                        reqEMVDataLen =
                            resultDataLength - (encCardNum16Len + cardDataIdx2 + noEncCardNumLen) - 2
                        emvData = ByteArray(reqEMVDataLen)
                        System.arraycopy(
                            resultData,
                            encCardNum16Len + cardDataIdx2 + noEncCardNumLen,
                            emvData,
                            0,
                            emvData.size
                        )
                        reqEMVData = ByteArray(emvData.size + 4)
                        System.arraycopy(
                            String.format(
                                "%04d",
                                *arrayOf<Any>(Integer.valueOf(emvData.size))
                            ).toByteArray(), 0, reqEMVData, 0, 4
                        )
                        System.arraycopy(emvData, 0, reqEMVData, 4, emvData.size)
                        hashReaderData["tradeCnt"] = tradeCnt.toByteArray()
                        hashReaderData["transType"] = "FB".toByteArray()
                        hashReaderData["reqEMVData"] = reqEMVData
                        hashReaderData["readerModelNum"] = readerModelNum

                        ksnetSocketCommunicationDTO.encryptInfo = encInfoLen
                        ksnetSocketCommunicationDTO.emvData = reqEMVData
                        ksnetSocketCommunicationDTO.readerModelNum = readerModelNum

                        deviceConnectSharedFlow.emit(DeviceConnectSharedFlow.RequestSocketCommunication(makeRequestTelegram(ksnetSocketCommunicationDTO)))

//                        clearTempBuffer()
//                        threadAdmission()
                    }
                }

                0xD6.toByte() -> {
                    if(isFirstPayment) {
                        if ((KsnetUtils().byteToString(resultData, 4, 3)) == "INS") {
                            isFirstPayment = false
//                            val data: ByteArray = EncMSRManager().makeCardNumSendReq(
//                                "00000001004".toByteArray(), "10".toByteArray()
//                            )
//                            serialCommunicate?.sendData(data)
//                            afterProcess.emit(
//                                ProcessAfterSerialCommunicate.ProcessValue(
//                                    ProcessAfterSerialCommunicate.RequestCardNumber.name))
                            deviceConnectSharedFlow.emit(
                                DeviceConnectSharedFlow.SerialCommunicationMessageFlow(SerialCommunicationMessage.PaymentProgressing.message)
                            )
//                            afterProcess.emit(ProcessAfterSerialCommunicate.RequestCardNumber)
                        }
//                            when(serialCommunicationInsetCardStatus) {
//                                SerialCommunicationInsertCardStatus.D0 -> {
//                                    serialCommunicationInsetCardStatus = null
//                                    val data: ByteArray = EncMSRManager.makeCardNumSendReq(
//                                        "000001004".toByteArray(), "10".toByteArray()
//                                    )
//                                    deviceSetting?.requestDeviceSerialCommunication(data)
//                                    serialCommunicationMessage.value =
//                                        Event(SerialCommunicationMessage.paymentProgressing.message)
//                                }
//                                SerialCommunicationInsertCardStatus.D5 -> {
//                                    serialCommunicationInsetCardStatus = null
//                                    serialCommunicationMessage.value =
//                                        Event(SerialCommunicationMessage.paymentProgressing.message)
//                                }
//                                else -> { }
//                            }
                    } else if ((KsnetUtils().byteToString(resultData, 4, 3)) == "DEL") {
                        if (false) {
//                                isCardInsertResponseDEL.value = Event(CardReadingType.MSR)
                        } else {
//                                isCardInsertResponseDEL.value = Event(CardReadingType.IC)
                        }
                    }
                }

                else -> {}
            }
        }
    }

    private fun receive(data: ByteArray): Int {
        System.arraycopy(data, 0, receiveData, receiveDataLength, data.size)
        receiveDataLength += data.size
        if (receiveDataLength > 3) {
            val bluetoothLength = KsnetUtils().byteToInt(receiveData[1]) * 256 + KsnetUtils().byteToInt(receiveData[2]) + 4
            val bluetoothLRC = receiveData[bluetoothLength - 2].toInt() == 3 &&
                    receiveData[bluetoothLength - 1] == checkLRC(KsnetUtils().byteToSubByte(receiveData, 0, bluetoothLength))
            val usbLength = KsnetUtils().byte2Int(receiveData[1]) * 0xff + KsnetUtils().byte2Int(receiveData[2])
            val usbLRC = receiveData != null && receiveData[usbLength + 2] == 0x03.toByte()

            if (bluetoothLength >= 1024 || usbLength >= 1024) {
                clearTempBuffer()
                return 0
            }
            if (bluetoothLRC || usbLRC) {
                var spn = "receive $receiveDataLength bytes\n"
                if (data.isNotEmpty())
                    spn += KsnetUtils().toHex(
                        receiveData,
                        receiveDataLength
                    )
                Log.w("responseData", spn)
                return 1
            }
            return 0
        }
        return 0
    }


    private fun isFallbackMessage(message: String): Boolean {
        for(fallbackMessage in FallbackCode.values()) {
            if(fallbackMessage.code == message) return true
        }
        return false
    }

    private fun getCommandID(t_data: ByteArray?, resultDataLength: Int): Byte {
        if (t_data == null || t_data.size < 6) {
            return 1
        }
        if (t_data[0].toInt() != 2) {
            return 2
        }
        return if (t_data[resultDataLength - 2].toInt() != 3) {
            3
        } else t_data[3]
    }

    private fun clearTempBuffer() {
        receiveDataLength = 0
        System.arraycopy(overWriteEmptyData, 0, receiveData, 0, 1024)
    }

    private fun checkLRC(bytes: ByteArray): Byte {
        var lrc: Byte = 0
        for (i in 1 until bytes.size - 1) {
            lrc = (bytes[i] xor lrc)
        }
        return lrc
    }

}