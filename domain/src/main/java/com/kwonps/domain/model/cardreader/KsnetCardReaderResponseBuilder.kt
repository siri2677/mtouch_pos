package com.kwonps.domain.model.cardreader

import java.nio.charset.StandardCharsets
import kotlin.experimental.and
import kotlin.experimental.xor

class KsnetCardReaderResponseBuilder {
    private val hashReaderData = HashMap<String, ByteArray>()
    private var receiveData = ByteArray(1024)
    private var overWriteEmptyData = ByteArray(2048)
    private var receiveDataLength = 0
    private var isFirstPayment = false

    enum class KsnetParsingByte(val value: Int) {
        IDX_COMMAND(3),
        IDX_DATA(4)
    }

    enum class FallbackCode(val code: String, val description: String) {
        FALLBACK_NO_ATR("01", "Chip 미 응답"),
        FALLBACK_NO_APPL("02", "Application 미 존재"),
        FALLBACK_READ_FAIL("03", "Chip 데이터 읽기 실패"),
        FALLBACK_NO_DATA("04", "Mandatory 데이터 미 포함"),
        FALLBACK_CVM_FAIL("05", "CVM 커맨드 응답실패"),
        FALLBACK_BAD_CM("06", "EMV 커맨드 오 설정"),
        FALLBACK_BAD_OPER("07", "터미널(리더기) 오 동작");
    }

    fun receiveData(data: ByteArray): CardReaderStatus.Communication {
        if (receive(data) == 1) {
            val resultData = ByteArray(1024)
            val resultDataLength = receiveDataLength
            System.arraycopy(receiveData, 0, resultData, 0, receiveData.size)
            clearTempBuffer()
            when (getCommandID(resultData, resultDataLength)) {
                0xD0.toByte() -> {
                    isFirstPayment = true
                    return CardReaderStatus.Communication.InsertIC
                }

                0xD2.toByte() -> {
                    var tradeCnt: String
                    var encInfoLen: Int
                    var encCardNum16Len: Int //암호화된 카드번호 필드 길이
                    var noEncCardNumLen: Int //암호화하지 않은 카드번호 길이
                    var reqEMVDataLen: Int //EMV 요청 Data 길이
                    var readerModelNum: ByteArray
                    var cardBin: ByteArray
                    var emvData: ByteArray
                    var trackII: ByteArray = " ".toByteArray()
                    val cardType = byteToString(resultData, KsnetParsingByte.IDX_DATA.value, 2)!!

                    isFirstPayment = false
                    if (FallbackCode.values().none { it.code == cardType }) {
                        if (cardType == "IC" || cardType == "MS") {
                            tradeCnt = byteToString(
                                resultData,
                                KsnetParsingByte.IDX_DATA.value + "IC".length,
                                3
                            )
                            readerModelNum = byteToString(
                                resultData,
                                KsnetParsingByte.IDX_DATA.value + 5,
                                16
                            ).toByteArray()
                            encInfoLen = byteToString(
                                resultData,
                                KsnetParsingByte.IDX_DATA.value + 5 + 16,
                                1
                            ).toByteArray()[0] + 1

                            //암호화 정보생성
                            val reqEncryptInfo = ByteArray(encInfoLen)
                            System.arraycopy(
                                resultData,
                                KsnetParsingByte.IDX_DATA.value + 5 + 16,
                                reqEncryptInfo,
                                0,
                                reqEncryptInfo.size
                            )

                            //EMV DATA
                            encCardNum16Len = byteToString(
                                resultData,
                                KsnetParsingByte.IDX_DATA.value + 5 + 16 + encInfoLen,
                                1
                            ).toByteArray()[0] + 1
                            if (encCardNum16Len === 0) encCardNum16Len = 1
                            noEncCardNumLen = byteToString(
                                resultData,
                                KsnetParsingByte.IDX_DATA.value + 5 + 16 + encInfoLen + encCardNum16Len,
                                1
                            ).toByteArray()[0] + 1 //암호화 하지 않은 카드번호 길이
                            val cardDataIdx: Int = KsnetParsingByte.IDX_DATA.value + "IC".length + tradeCnt.length + readerModelNum.size + encInfoLen //카드데이터 필드 인덱스

                            if (cardType == "IC") {
                                reqEMVDataLen = resultDataLength - (cardDataIdx + encCardNum16Len + noEncCardNumLen) - 2 //EMV DATA 길이
                                cardBin = ByteArray(noEncCardNumLen - 2)
                                System.arraycopy(
                                    resultData,
                                    cardDataIdx + encCardNum16Len + 2,
                                    cardBin,
                                    0,
                                    cardBin.size
                                )

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

                                return CardReaderStatus.Communication.result(
                                    readerModelNum = readerModelNum,
                                    encryptInfo = reqEncryptInfo,
                                    reqEMVData = reqEMVData,
                                    cardNumber = String(cardBin, StandardCharsets.UTF_8),
                                    trackII = trackII
                                )
                            }
                        }
                    } else {
                        val fallbackCode: FallbackCode? = FallbackCode.values().find { it.code == cardType }

                        return CardReaderStatus.Communication.FallBack(
                            description = fallbackCode?.description.let { it } ?: "",
                            code = fallbackCode?.code.let { it } ?: ""
                        )
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

                    if(byteToString(resultData, KsnetParsingByte.IDX_DATA.value, 2) == ("FB")){
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
                    }
                }

                0xD6.toByte() -> {
                    if(isFirstPayment) {
                        if ((byteToString(resultData, 4, 3)) == "INS") {
                            isFirstPayment = false
                            return CardReaderStatus.Communication.ReadingIC
                        }
                    }
                }
            }
        }
        return CardReaderStatus.Communication.Init
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
    }

    private fun byteToString(buf: ByteArray, start: Int, size: Int): String {
        try {
            return String(buf!!, start, size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
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