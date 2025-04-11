package com.example.domain.model.cardreader


import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.user.UserDetailData
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.experimental.and

class KsnetCardReaderRequestBuilder {
    object PrinterCommands {
        val ESC_FONT_COLOR_DEFAULT: ByteArray = byteArrayOf(27, 114, 0)
        val FS_FONT_ALIGN: ByteArray = byteArrayOf(28, 33, 1, 27, 33, 1)
        val ESC_ALIGN_LEFT: ByteArray = byteArrayOf(27, 97, 0)
        val ESC_ALIGN_CENTER = byteArrayOf(27, 97, 1)
        val ESC_ALIGN_RIGHT = byteArrayOf(27, 97, 2)
        val ESC_CANCEL_BOLD: ByteArray = byteArrayOf(27, 69, 0)
        const val LF: Byte = 10
    }

    private val yymmddhhmmss = getTime()!!.substring(0, 12)
    private val stx: Byte = 0x02
    private val etx: Byte = 0x03.toByte()
    private val ksnet_dongle_info_req = 0xC0.toByte()
    private val KSNET_CARDNO_REQ = 0xC2.toByte()
    private val KSNET_FALLBACK_REQ = 0xC5.toByte()

    private var packet = ByteArray(1024)
    private var year: String? = getTime()?.substring(0, 2)

    private fun getTime(): String? {
        val time = System.currentTimeMillis()
        val dayTime = SimpleDateFormat("yyMMddhhmmss")
        return dayTime.format(Date(time))
    }

    private fun lrc(bytes: ByteArray, length: Int): Int {
        var checksum = 0
        for (i in 1 until length) {
            checksum = checksum xor ((bytes[i] and 0xFF.toByte()).toInt())
        }
        return checksum
    }

    fun initDevice(): ByteArray {
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
        packet[idx++] = lrc(packet, idx).toByte()
        val txPacket = ByteArray(idx)
        System.arraycopy(packet, 0, txPacket, 0, idx)

        return txPacket
    }

    fun makeCardNumSendReq(totalAmount: ByteArray, resTime: ByteArray): ByteArray {
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
        fallbackErrCode: String,
        timeOut: String,
    ): ByteArray {
        var idx = 0
        packet[idx++] = stx
        System.arraycopy(make2ByteLengh(25), 0, packet, idx, 2)
        val idx2 = idx + 2
        val idx3 = idx2 + 1
        packet[idx2] = KSNET_FALLBACK_REQ
        System.arraycopy(yymmddhhmmss.toByteArray(), 0, packet, idx3, 12)
        val idx4 = idx3 + 12
        System.arraycopy(
            String.format("%09d", *arrayOf<Any>(Integer.valueOf(fallbackErrCode.toInt())))
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

    private fun make2ByteLengh(i: Int): ByteArray? {
        val bArr = ByteArray(2)
        bArr[1] = i.toByte()
        bArr[0] = (i ushr 8).toByte()
        return bArr
    }

    fun printMode(): ByteArray {
        val printModeCommand = ByteArray(6)
        var idx = 0
        printModeCommand[idx++] = 0x02
        printModeCommand[idx++] = 0x00
        printModeCommand[idx++] = 0x00
        printModeCommand[idx++] = 0x10 // 프린터모드 Command
        printModeCommand[idx++] = 0x03
        val bLRC = LRC(printModeCommand, idx)
        printModeCommand[idx] = bLRC.toByte() // LRC
        return printModeCommand
    }

    fun resetPrint(): ByteArray {
        val baos = ByteArrayOutputStream()
        try {
            baos.apply {
                write(PrinterCommands.ESC_FONT_COLOR_DEFAULT)
                write(PrinterCommands.FS_FONT_ALIGN)
                write(PrinterCommands.ESC_ALIGN_LEFT)
                write(PrinterCommands.ESC_CANCEL_BOLD)
                write(PrinterCommands.LF.toInt())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            Thread.sleep(20)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return baos.toByteArray()
    }

    fun printReceipt(
        userDetailData: UserDetailData,
        paymentDetailData: PaymentDetailData
    ): ByteArray {
        val baos = ByteArrayOutputStream()

        baos.writeLine("가 맹 점 명: ${userDetailData.mchtName}\n")

//        try {
//            baos.writeLine("[${paymentDetailData.cardType ?: "신용카드"}전표]\n\n\n", 2, 1)
//
//            baos.writeLine("가 맹 점 명: ${userDetailData.mchtName}\n")
//            baos.writeLine("대 표 자 명: ${userDetailData.ceoName}\n")
//            baos.writeLine("사업자 번호: ${userDetailData.identity}\n")
//            baos.writeLine("전 화 번 호: ${userDetailData.telNo}\n")
//            baos.writeLine("주       소: ${userDetailData.address}\n")
//            baos.writeLine("- - - - - - - - - - - - - - - - - - - - - - - \n")
//
//            baos.writeLine("*** 신용승인정보 ***\n\n", 2, 1)
//
//            // 거래일시
//            baos.writeLine("거래  일시: ${paymentDetailData.authDate}\n")
//            baos.writeLine("카드  종류: ${paymentDetailData.purchaseName}\n")
//            baos.writeLine("카드  번호: ${paymentDetailData.cardType}\n")
//
//            if (paymentDetailData.installment.toInt() == 0) {
//                baos.writeLine("할부  개월: 일시불\n")
//            } else {
//                baos.writeLine("할부  개월: ${paymentDetailData.installment}개월\n")
//            }
//            baos.writeLine("전표매입사: ${paymentDetailData.issuerName}}\n")
//
//            if (paymentDetailData.trxResult == "0200") {
//                baos.writeLine("판매  금액: ", 2, 0)
//                baos.writeLine("${paymentDetailData.supplyAmount}원\n", 2, 2)
//                baos.writeLine("부  가  세: ", 2, 0)
//                baos.writeLine("${paymentDetailData.taxAmount}원\n", 2, 2)
//                baos.writeLine("승인  금액: ", 2, 0)
//                baos.writeLine("${paymentDetailData.totalAmount}원\n", 2, 2)
//            } else {
//               baos.writeLine("판매  금액: ", 2, 0)
//               baos.writeLine("-${paymentDetailData.supplyAmount}원\n", 2, 2)
//               baos.writeLine("부  가  세: ", 2, 0)
//               baos.writeLine("-${paymentDetailData.taxAmount}원\n", 2, 2)
//               baos.writeLine("승인  금액: ", 2, 0)
//               baos.writeLine("-${paymentDetailData.totalAmount}원\n", 2, 2)
//            }
//
//            paymentDetailData.remainAmount?.let {
//                if ((paymentDetailData.cardType?.contains("Gift") == true || paymentDetailData.cardType?.contains("선불") == true)
//                        && paymentDetailData.remainAmount.trim() != "") {
//                    baos.writeLine("잔      액: ", 2, 0)
//                    baos.writeLine("${paymentDetailData.remainAmount.toInt()}\n", 2, 2)
//                }
//            }
//
//            baos.writeLine("\n", 2, 2)
//            baos.writeLine("승인  번호: ")
//            baos.writeLine("${paymentDetailData.authCode}\n", 0, 2)
//
//            baos.writeLine("- - - - - - - - - - - - - - - - - - - - - - - \n")
//            baos.writeLine("*감사합니다*\n\n\n\n\n", 0, 1)
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

        return baos.toByteArray()
    }

    fun printCustomToByteArray(msg: String, size: Int, align: Int): ByteArray {
        val baos = ByteArrayOutputStream()

        val cc = byteArrayOf(0x1B, 0x21, 0x00)  // Normal
        val bb = byteArrayOf(0x1B, 0x21, 0x08)  // Bold
        val bb2 = byteArrayOf(0x1B, 0x21, 0x20) // Bold + Medium
        val bb3 = byteArrayOf(0x1B, 0x21, 0x10) // Bold + Large

        try {
            when (size) {
                0 -> baos.write(cc)
                1 -> baos.write(bb)
                2 -> baos.write(bb2)
                3 -> baos.write(bb3)
            }

            when (align) {
                0 -> baos.write(PrinterCommands.ESC_ALIGN_LEFT)
                1 -> baos.write(PrinterCommands.ESC_ALIGN_CENTER)
                2 -> baos.write(PrinterCommands.ESC_ALIGN_RIGHT)
            }

            baos.write(msg.toByteArray(charset("euc-kr")))

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return baos.toByteArray()
    }

    fun ByteArrayOutputStream.writeLine(text: String, align: Int = 0, font: Int = 0) {
        write(printCustomToByteArray(text, align, font))
    }

    fun LRC(bytes: ByteArray, length: Int): Int {
        var checksum = 0
        for (i in 1 until length) {
            checksum = checksum xor (bytes[i].toInt() and 0xFF)
        }
        return checksum
    }
}