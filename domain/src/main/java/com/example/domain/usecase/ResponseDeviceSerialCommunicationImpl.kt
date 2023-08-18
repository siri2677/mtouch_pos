package com.example.domain.usecase

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.domain.enumclass.CardReadingType
import com.example.domain.enumclass.DeviceType
import com.example.domain.enumclass.FallbackMessage
import com.example.domain.enumclass.KsnetParsingByte
import com.example.domain.enumclass.SerialCommunicationInsertCardStatus
import com.example.domain.enumclass.SerialCommunicationMessage
import com.example.domain.usecase.bluetooth.BluetoothDeviceSetting
import com.example.domain.usecase.usb.UsbDeviceSetting
import com.example.domain.usecaseinterface.DeviceSetting
import com.example.domain.usecaseinterface.ResponseDeviceSerialCommunication
import com.ksnet.interfaces.Approval
import com.mtouch.ksr02_03_04_v2.Domain.Model.EncMSRManager
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event
import kotlinx.coroutines.delay
import kotlin.experimental.xor


class ResponseDeviceSerialCommunicationImpl : ResponseDeviceSerialCommunication {
    private var context: Context? = null
    private var deviceSetting: DeviceSetting? = null
    private var receiveData = ByteArray(1024)
    private var overWriteEmptyData = ByteArray(2048)
    private var receiveDataLength = 0

    private var isFirstPayment = false
    private var isFallBackOccur = false
    private var serialCommunicationInsetCardStatus: SerialCommunicationInsertCardStatus? = null
//    private var icGubuFallback = ""

    private val hashReaderData = HashMap<String, ByteArray>()

    override val serialCommunicationMessage = MutableLiveData<Event<String?>?>()
    override val isCompletePayment= MutableLiveData<Event<Boolean?>?>()


    val isCardInsertResponseINS = MutableLiveData<Event<Boolean>?>()
    val isCardInsertResponseDEL = MutableLiveData<Event<CardReadingType>?>()
    val isCardInsertResponseError = MutableLiveData<Event<Boolean>?>()


    override fun setDeviceSetting(context: Context, deviceSetting: DeviceSetting): ResponseDeviceSerialCommunicationImpl {
        this.context = context
        this.deviceSetting = deviceSetting
        return this
    }

    override fun receiveData(data: ByteArray) {
        if (receive(data) == 1) {
            val resultData = ByteArray(1024)
            val resultDataLength = receiveDataLength
            System.arraycopy(receiveData, 0, resultData, 0, receiveData.size)
            clearTempBuffer()
            val cmd: Byte = getCommandID(resultData, resultDataLength)
            when (cmd) {
//                   0xDF.toByte() -> {
////                       test()
//                       clearTempBuffer()
//                       usbDeviceDisConnect()
//                       btDeviceDisConnect()
//                   }
                0xD0.toByte() -> {
//                    serialCommunicationInsetCardStatus = SerialCommunicationInsertCardStatus.D0
                    isFirstPayment = true
                    serialCommunicationMessage.value = Event(SerialCommunicationMessage.icCardInsertRequest.message)
//                    clearTempBuffer()
//                    deviceSetting?.requestDeviceSerialCommunication(EncMSRManager.makeRequestCardInOutStatus())
//                       test()
                }

                0x06.toByte() -> {
//                       test1()
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
//                    val FallBack_ErrCode: String = EncMSRManager.chkFallBack(cardType)

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
                            hashReaderData["EncryptInfo"] = reqEncryptInfo

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
                            val cardDataIdx: Int =
                                KsnetParsingByte.IDX_DATA.keyValue() + "IC".length + tradeCnt.length + readerModelNum.size + encInfoLen //카드데이터 필드 인덱스
                            hashReaderData["tradeCnt"] = tradeCnt.toByteArray()
                            hashReaderData["readerModelNum"] = readerModelNum
                            hashReaderData["transType"] = "IC".toByteArray()
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
                                reqEMVDataLen =
                                    resultDataLength - (cardDataIdx + encCardNum16Len + noEncCardNumLen) - 2 //EMV DATA 길이
                                cardBin = ByteArray(noEncCardNumLen - 2)
                                System.arraycopy(
                                    resultData,
                                    cardDataIdx + encCardNum16Len + 2,
                                    cardBin,
                                    0,
                                    cardBin.size
                                )
                                hashReaderData["Cardbin"] = cardBin
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
                                hashReaderData["reqEMVData"] = reqEMVData
                                hashReaderData["trackII"] = " ".toByteArray()
//                                clearTempBuffer()
                                threadAdmission()

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
                        serialCommunicationInsetCardStatus = SerialCommunicationInsertCardStatus.D5
                        val data: ByteArray = EncMSRManager.makeFallBackCardReq(cardType, "99")
                        deviceSetting?.requestDeviceSerialCommunication(data)
                        Log.w("fallbackMessage", fallbackMessage(cardType))
                        serialCommunicationMessage.value = Event(SerialCommunicationMessage.fallBackMessage.fallbackDetail(fallbackMessage(cardType)))
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
                    if(KsnetUtils().byteToString(resultData, 4, 2) == "00") {
                        serialCommunicationMessage.value = Event(SerialCommunicationMessage.completePayment.message)
//                        clearTempBuffer()
                        Log.w("0xD3", "0xD3")
                    }
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

//                        clearTempBuffer()
                        threadAdmission()
                    }
                }

                0xD6.toByte() -> {
                    if(isFirstPayment) {
                        if ((KsnetUtils().byteToString(resultData, 4, 3)) == "INS") {
                            isFirstPayment = false
                            serialCommunicationInsetCardStatus = null
                            val data: ByteArray = EncMSRManager.makeCardNumSendReq(
                                "000001004".toByteArray(), "10".toByteArray()
                            )
                            deviceSetting?.requestDeviceSerialCommunication(data)
                            serialCommunicationMessage.value =
                                Event(SerialCommunicationMessage.paymentProgressing.message)
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
                                isCardInsertResponseDEL.value = Event(CardReadingType.MSR)
                            } else {
                                isCardInsertResponseDEL.value = Event(CardReadingType.IC)
                            }
                        }
                }

                else -> {}
            }
        }
    }

    fun threadAdmission() {
        val bEncSign: ByteArray? = null
        val mchtDataField = ByteArray(30)
        for (i in 0..29) mchtDataField[i] = " ".toByteArray()[0]
        val telegramNo = KsnetUtils().generateString(12)?.toByteArray()
        val responseTelegram = ByteArray(2048)
        val requestTelegram = EncMSRManager.makeRequestTelegram(
            "IC".toByteArray(), //adminInfo.getTransType(),
            "0420".toByteArray(), //adminInfo.getTelegramType(),
            "01".toByteArray(), //adminInfo.getWorkType(),
            telegramNo!!,
            "S".toByteArray(), //adminInfo.getPosEntry(),
            "30014624".toByteArray(), //bAdminNum,
            "230811".toByteArray(), //bAdmindate,
            mchtDataField,
            "DPT0A24658".toByteArray(), //adminInfo.getDPTID(),
            "######MTOUCH1101".toByteArray(),
            "######KSR-051101".toByteArray(),
            "00".toByteArray(), //adminInfo.getPayType(),
            "000000001004".toByteArray(), //adminInfo.getTotalAmount(),
            "000000000913".toByteArray(), //adminInfo.getAmount(),
            "000000000000".toByteArray(), //adminInfo.getServicAmount(),
            "000000000091".toByteArray(),
            "000000000000".toByteArray(), //adminInfo.getFreeAmount(),
            "".toByteArray(), //adminInfo.getFiller(),
            "N".toByteArray(), //adminInfo.getSignTrans(),
            bEncSign,
            hashReaderData["EncryptInfo"],
            hashReaderData["reqEMVData"],
            hashReaderData["trackII"]
        )

        Thread {
            val rtn: Int = Approval().request(
                "210.181.28.137",
                9562,
                5,
                requestTelegram,
                responseTelegram,
                16000
            )

            if (rtn < 0 && rtn != -102 && rtn != -103 && rtn != -104) {
                Log.w("threadAdmmision", "threadAdmmision")
            }
            KsnetUtils().reqDataPrint(requestTelegram)
            KsnetUtils().respGetHashData(responseTelegram)

            if (rtn >= 0) {
                serialCommunicationMessage.postValue(Event(SerialCommunicationMessage.completePayment.message))
                Handler(Looper.getMainLooper()).postDelayed({
                    isCompletePayment.postValue(Event(true))
                },2000)
//                val req2thGenerate = EncMSRManager.make2ThGenerateReq(
//                    "000001004".toByteArray(), "00".toByteArray(),
//                    hashReaderData["tradeCnt"]!!, hashReaderData["reqEMVData"]!!
//                )
//                deviceSetting?.requestDeviceSerialCommunication(req2thGenerate)
            }
        }.start()
    }

    override fun init() {
        isFirstPayment = false
    }

    fun isFallbackMessage(message: String): Boolean {
        for(fallbackMessage in FallbackMessage.values()) {
             if(fallbackMessage.keyValue() == message) return true
        }
        return false
    }

    fun fallbackMessage(message: String): String {
        for(fallbackMessage in FallbackMessage.values()) {
            if(fallbackMessage.keyValue() == message) fallbackMessage.toString()
        }
        return ""
    }

    private fun receive(data: ByteArray): Int {
        var length: Int? = null
        var LRC: Boolean? = null
        System.arraycopy(data, 0, receiveData, receiveDataLength, data.size)
        receiveDataLength += data.size
        if (receiveDataLength > 3) {
            when (deviceSetting) {
                is UsbDeviceSetting -> {
                    length = KsnetUtils().byteToInt(receiveData[1]) * 256 + KsnetUtils().byteToInt(receiveData[2]) + 4
                    LRC = receiveData[length - 2].toInt() == 3 &&
                            receiveData[length - 1] == checkLRC(KsnetUtils().byteToSubByte(receiveData, 0, length))
                }

                is BluetoothDeviceSetting -> {
                    length = KsnetUtils().byte2Int(receiveData[1]) * 0xff + KsnetUtils().byte2Int(receiveData[2])
                    LRC = receiveData != null && receiveData[length + 2] == 0x03.toByte()
                }
            }
            if (length!! >= 1024) {
                clearTempBuffer()
                return 0
            }
            if (LRC!!) {
                val spn = SpannableStringBuilder()
                spn.append("receive $receiveDataLength bytes\n")
                if (data.isNotEmpty()) spn.append(
                    KsnetUtils().toHex(
                        receiveData,
                        receiveDataLength
                    )
                ).append("\n")
                Log.w("receiveData", spn.toString())
                return 1
            }
            return 0
        }
        return 0
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