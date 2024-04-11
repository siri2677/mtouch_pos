package com.example.mtouchpos.device

import android.util.Log
import com.example.mtouchpos.FlowManager
import com.example.mtouchpos.vo.DeviceSerialCommunicate
import com.example.mtouchpos.dto.SerialInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import kotlin.experimental.and
import kotlin.experimental.xor

class ResponseSerialCommunicationFormat {
    private val hashReaderData = HashMap<String, ByteArray>()
    private var receiveData = ByteArray(1024)
    private var overWriteEmptyData = ByteArray(2048)
    private var receiveDataLength = 0
    private var isFirstPayment = false

    enum class KsnetParsingByte(val value: Int) {
        IDX_COMMAND(3), IDX_DATA(4);
        fun keyValue(): Int {
            return value
        }
    }

    enum class FallbackCode(val code: String) {
        Code01("01"),
        Code02("02"),
        Code03("03"),
        Code04("04"),
        Code05("05"),
        Code06("06"),
        Code07("07"),
        Code08("08")
    }

    fun receiveData(data: ByteArray) {
        if (receive(data) == 1) {
            val resultData = ByteArray(1024)
            val resultDataLength = receiveDataLength
            System.arraycopy(receiveData, 0, resultData, 0, receiveData.size)
            clearTempBuffer()
            val cmd: Byte = getCommandID(resultData, resultDataLength)
            when (cmd) {
                0xD0.toByte() -> {
                    isFirstPayment = true
                    CoroutineScope(Dispatchers.IO).launch {
                        FlowManager.deviceSerialCommunicate.emit(
                            DeviceSerialCommunicate.SerialCommunicationMessage.IcCardInsertRequest()
                        )
                    }

//                    afterProcess.emit(ProcessAfterSerialCommunicate.RequestCardInsert)


//                    afterProcess.emit(emit
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
                    var trackII: ByteArray = " ".toByteArray()
                    val cardType = byteToString(resultData, KsnetParsingByte.IDX_DATA.keyValue(), 2)!!

                    isFirstPayment = false
                    if (!isFallbackMessage(cardType)) {   // 정상적인 응답인 경우
                        if (cardType == "IC" || cardType == "MS") {
                            tradeCnt = byteToString(
                                resultData,
                                KsnetParsingByte.IDX_DATA.keyValue() + "IC".length,
                                3
                            )
                            readerModelNum = byteToString(
                                resultData,
                                KsnetParsingByte.IDX_DATA.keyValue() + 5,
                                16
                            ).toByteArray()
                            encInfoLen = byteToString(
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
                            encCardNum16Len = byteToString(
                                resultData,
                                KsnetParsingByte.IDX_DATA.keyValue() + 5 + 16 + encInfoLen,
                                1
                            ).toByteArray()[0] + 1
                            if (encCardNum16Len === 0) encCardNum16Len = 1
                            noEncCardNumLen = byteToString(
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
                                cardBin = ByteArray(noEncCardNumLen - 2)
                                System.arraycopy(
                                    resultData,
                                    cardDataIdx + encCardNum16Len + 2,
                                    cardBin,
                                    0,
                                    cardBin.size
                                )
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
//                                requestKsnetSocketCommunicateDTO.readerModelNum = readerModelNum
//                                requestKsnetSocketCommunicateDTO.encryptInfo = reqEncryptInfo
//                                requestKsnetSocketCommunicateDTO.reqEMVData = reqEMVData
//                                requestKsnetSocketCommunicateDTO.cardNumber = String(cardBin, StandardCharsets.UTF_8)

                                CoroutineScope(Dispatchers.IO).launch {
                                    FlowManager.deviceSerialCommunicate.emit(
                                        DeviceSerialCommunicate.RequestSocketCommunication(
                                            SerialInfo(
                                                readerModelNum = readerModelNum,
                                                encryptInfo = reqEncryptInfo,
                                                reqEMVData = reqEMVData,
                                                cardNumber = String(cardBin, StandardCharsets.UTF_8),
                                                trackII = trackII
                                            )
                                        )
//                                        DeviceSerialCommunicate.RequestSocketCommunication(requestKsnetSocketCommunicateDTO)
                                    )
                                }
//
//                                val responseTelegram = ByteArray(2048)
//                                val rtn: Int = Approval().request(
//                                    "210.181.28.137",
//                                    9562,
//                                    5,
//                                    makeRequestTelegram(
//                                        ksnetSocketCommunicationDTO = requestKsnetSocketCommunicateDTO
//                                    ),
//                                    responseTelegram,
//                                    16000
//                                )
//
//                                KsnetUtils().reqDataPrint(
//                                    makeRequestTelegram(
//                                        ksnetSocketCommunicationDTO = requestKsnetSocketCommunicateDTO
//                                    )
//                                )
//                                KsnetUtils().respGetHashData(responseTelegram)
//
//                                fun byteToString(srcBytes: ByteArray, startIdx: Int, len: Int): String {
//                                    if (startIdx + len > srcBytes.size) {
//                                        return "~~~"
//                                    }
//                                    val arrByte = ByteArray(len)
//                                    System.arraycopy(srcBytes, startIdx, arrByte, 0, len)
//                                    return try {
//                                        String(arrByte, charset("EUC-KR"))
//                                    } catch (e: Exception) {
//                                        e.printStackTrace()
//                                        return "X"
//                                    }
//                                }
//
//                                if(byteToString(responseTelegram, 40, 1) == "X") {
//                                    Log.w("fail11", "responseTelegram")
//                                }
//                                if(byteToString(responseTelegram, 40, 1) == "O") {
//                                    Log.w("success", "responseTelegram")
//                                }
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
                        CoroutineScope(Dispatchers.IO).launch {
                            FlowManager.deviceSerialCommunicate.emit(
                                DeviceSerialCommunicate.SerialCommunicationMessage.FallBackMessage().setData(cardType)
                            )
                        }
//                        afterProcess.emit(ProcessAfterSerialCommunicate.RequestFallback(cardType))
                    }
                    //IC우선거래가 아닌 일반 MS 거래시 거래진행
//                            if (ICGubu.equals("MS")) {
////                                    LOG.w("tag data","recvData: "+new String(resultData));
////                                    LOG.w("tag data","recvData: "+new String(KsnetUtils.byteToString(resultData, IDX_DATA + 5 + 16 + encInfoLen + encCardNum16Len, noEncCardNumLen-1)));
////                                    LOG.w("tag data","receiptNo: "+new String(adminInfo.getReceiptNo()) +" length: "+adminInfo.getReceiptNo().length+" value: "+adminInfo.getReceiptNo());
//                                if (!isFallback &&
//                                    (adminInfo.getReceiptNo().length !== 0
//                                            && KsnetUtils.byteToString(
//                                        resultData,
//                                        IDX_DATA + 5 + 16 + encInfoLen + encCardNum16Len + noEncCardNumLen - 1,
//                                        1
//                                    ).equals("2") ||
//                                            KsnetUtils.byteToString(
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
//                                    trackIILen = KsnetUtils.byteToString(
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
                    Log.w("0xD3", byteToString(resultData, 4, 2))
//                    if(byteToString(resultData, 4, 2) == "00") {
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

                    if(byteToString(resultData, KsnetParsingByte.IDX_DATA.keyValue(), 2) == ("FB")){
                        tradeCnt = byteToString(resultData, "FB".length + 4, 3)
                        readerModelNum = byteToString(resultData, 9, 16).toByteArray()

                        requestEncInfoLen = byteToString(resultData, 25, 1).toByteArray()[0] + 1
                        encInfoLen = ByteArray(requestEncInfoLen)
                        System.arraycopy(resultData, 25, encInfoLen, 0, encInfoLen.size)
                        hashReaderData["EncryptInfo"] = encInfoLen

                        encCardNum16Len =
                            byteToString(resultData, requestEncInfoLen + 25, 1).toByteArray()[0] + 1
                        noEncCardNumLen =
                            byteToString(resultData, requestEncInfoLen + 25 + encCardNum16Len, 1)
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

//                        ksnetSocketCommunicationDTO.readerModelNum = readerModelNum
//                        ksnetSocketCommunicationDTO.encryptInfo = encInfoLen
//                        ksnetSocketCommunicationDTO.emvData = reqEMVData
//                        ksnetSocketCommunicationDTO.cardBin = String(cardBin, StandardCharsets.UTF_8)

//                        CoroutineScope(Dispatchers.IO).launch {
//                            SharedFlowManager.deviceSerialCommunicate.emit(
//                                DeviceSerialCommunicate.RequestSocketCommunication(responseSerialCommunicateDTO)
//                            )
//                        }

//                        clearTempBuffer()
//                        threadAdmission()
                    }
                }

                0xD6.toByte() -> {
                    if(isFirstPayment) {
                        if ((byteToString(resultData, 4, 3)) == "INS") {
                            isFirstPayment = false
//                            val data: ByteArray = EncMSRManager().makeCardNumSendReq(
//                                "00000001004".toByteArray(), "10".toByteArray()
//                            )
//                            serialCommunicate?.sendData(data)
//                            afterProcess.emit(
//                                ProcessAfterSerialCommunicate.ProcessValue(
//                                    ProcessAfterSerialCommunicate.RequestCardNumber.name))

                            CoroutineScope(Dispatchers.IO).launch {
                                FlowManager.deviceSerialCommunicate.emit(
                                    DeviceSerialCommunicate.SerialCommunicationMessage.PaymentProgressing()
                                )
                            }
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
                    } else if ((byteToString(resultData, 4, 3)) == "DEL") {
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
            val bluetoothLength = byteToInt(receiveData[1]) * 256 + byteToInt(receiveData[2]) + 4
            val bluetoothLRC = receiveData[bluetoothLength - 2].toInt() == 3 &&
                    receiveData[bluetoothLength - 1] == checkLRC(byteToSubByte(receiveData, 0, bluetoothLength))
            val usbLength = byteToInt(receiveData[1]) * 0xff + byteToInt(receiveData[2])
            val usbLRC = receiveData != null && receiveData[usbLength + 2] == 0x03.toByte()

            if (bluetoothLength >= 1024 || usbLength >= 1024) {
                clearTempBuffer()
                return 0
            }
            if (bluetoothLRC || usbLRC) {
                var spn = "receive $receiveDataLength bytes\n"
                if (data.isNotEmpty()) spn += toHex(receiveData, receiveDataLength)
                Log.w("responseData", spn)
                return 1
            }
            return 0
        }
        return 0
    }

    private fun byteToSubByte(buf: ByteArray, start: Int, length: Int): ByteArray {
        val _buf = ByteArray(length)
        if (start + length > buf.size) {
            return _buf
        }
        System.arraycopy(buf, start, _buf, 0, length)
        return _buf
        //   return  Arrays.copyOfRange(buf, start, end);
    }

    private fun byteToString(buf: ByteArray, start: Int, size: Int): String {
        try {
            return String(buf!!, start, size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun byte2Int(src: Byte): Int {
        return src.toInt() and 0xFF
    }

    private fun byteToInt(b: Byte): Int {
        return b.toInt() and 0xFF
    }

    fun toHex(buf: ByteArray?, idx: Int): String? {
        val HEX = "0123456789ABCDEF"
        if (buf == null) {
            return ""
        }
        val result = StringBuffer(buf.size * 2)
        for (i in 0 until idx) {
            result.append(HEX[buf[i].toInt() shr 4 and 15]).append(HEX[(buf[i] and 15).toInt()])
        }
        return result.toString()
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