package com.example.mtouchpos.print

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment.getExternalStorageDirectory
import com.pos.sdk.printer.PosPrinter
import com.example.domain.model.payment.OfflinePaymentData
import com.example.mtouchpos.vo.info.ApprovedPaymentType
import com.example.mtouchpos.vo.info.UserInfo
import com.example.mtouchpos.vo.type.PurchaseType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.Serializable
import kotlin.String
import kotlin.let

class PrintXPDA(
    private val userInfo: UserInfo,
    private val approvedPaymentType: ApprovedPaymentType
): CardTerminalPrintManager, Serializable {
    private val printObject = Object()
    private val maxLine = 70
    private val posPrinter = PosPrinter.open()
    private val printerInfo = PrinterInfo()

    enum class DataFormat {
        ALIGN_LEFT,
        ALIGN_RIGHT,
        ALIGN_CENTER
    }

    class PrinterInfo {
        var printLineCount: Int = 0   // 인쇄할 라인 수
        var colPerLine: Int           // 한 라인당 칼럼 수
        var isPrintStatus: Boolean = false // 인쇄 상태 확인

        val MAX_COLUMN_42 = 42
        val MAX_COLUMN_32 = 32

        init {
            colPerLine = MAX_COLUMN_42
        }
    }

    override fun invoke() {
        Thread {
            try {
                // 라인 추가시 마다 라인카운드 값은 1씩 증가됨.
                printerInfo.printLineCount = 0

//            val mPosPrintStateInfo = PosPrinter.getPrintStateInfo(0)
                posPrinter.cleanCache()  // 프린터 메모리 클리어
                val param = posPrinter.parameters  // 설정값 가져오기

                param.apply {
                    fontSize = 24  // 라인당 42 칼럼 문자
                    printGray = 2000
                    fontEffet = 0  // Font Effect 없음
                    lineSpace = 5

                    val fontFile = File("/system/fonts/XPDA-A42CGulim.ttf")
                    fontName = if (!fontFile.exists()) {
                        "${getExternalStorageDirectory()}/fonts/XPDA-A42CGulim.ttf"
                    } else {
                        "/system/fonts/XPDA-A42CGulim.ttf"
                    }
                }
                printerInfo.colPerLine = printerInfo.MAX_COLUMN_42
                posPrinter.parameters = param

                if (!addPrintLine("- - - - - - - - - - - - - - - - - - - - - ")) return@Thread

                var sPrint1 = "가 맹 점 명:"
                var sPrint2 = userInfo.mchtName
                var sPrintLine = printLine(
                    listOf(
                        format(sPrint1, 15, " ", DataFormat.ALIGN_LEFT),
                        format(sPrint2, 27, " ", DataFormat.ALIGN_RIGHT)
                    )
                )
                if (!addPrintLine(sPrintLine)) return@Thread

                sPrint1 = "대 표 자 명:"
                sPrint2 = userInfo.ceoName
                sPrintLine = printLine(
                    listOf(
                        format(sPrint1, 15, " ", DataFormat.ALIGN_LEFT),
                        format(sPrint2, 27, " ", DataFormat.ALIGN_RIGHT)
                    )
                )
                if (!addPrintLine(sPrintLine)) return@Thread

                sPrint1 = "사업자 번호: "
                sPrint2 = userInfo.identity
                sPrintLine = printLine(
                    listOf(
                        format(sPrint1, 15, " ", DataFormat.ALIGN_LEFT),
                        format(sPrint2, 27, " ", DataFormat.ALIGN_RIGHT)
                    )
                )
                if (!addPrintLine(sPrintLine)) return@Thread

                sPrint1 = "전 화 번 호: "
                sPrint2 = userInfo.telNo
                sPrintLine = printLine(
                    listOf(
                        format(sPrint1, 15, " ", DataFormat.ALIGN_LEFT),
                        format(sPrint2, 27, " ", DataFormat.ALIGN_RIGHT)
                    )
                )
                if (!addPrintLine(sPrintLine)) return@Thread

                sPrint1 = "주 소: "
                sPrint2 = userInfo.address
                sPrintLine = printLine(
                    listOf(
                        format(sPrint1, 15, " ", DataFormat.ALIGN_LEFT),
                        format(sPrint2, 27, " ", DataFormat.ALIGN_RIGHT)
                    )
                )
                if (!addPrintLine(sPrintLine)) return@Thread

                if (!addPrintLine("- - - - - - - - - - - - - - - - - - - - - ")) return@Thread

                param.apply {
                    fontSize = 23
                    fontFlags = 0
                    printAlign = 1
                }
                printerInfo.colPerLine = printerInfo.MAX_COLUMN_42
                posPrinter.parameters = param

                when(approvedPaymentType) {
                    is ApprovedPaymentType.CompletePaymentViewInfo -> {
                        if (!addPrintLine("** 신용${if (approvedPaymentType.purchaseType == PurchaseType.REFUND) "취소" else "승인"}정보 **")) return@Thread
                        if (!addNewLine(1)) return@Thread

                        param.apply {
                            fontSize = 24
                            fontEffet = 0
                            printAlign = 0
                        }
                        printerInfo.colPerLine = printerInfo.MAX_COLUMN_42
                        posPrinter.parameters = param

                        sPrint1 = "거래 일시: "
                        sPrint2 = approvedPaymentType.authDate
                        sPrintLine = printLine(
                            listOf(
                                format(sPrint1, 15, " ", DataFormat.ALIGN_LEFT),
                                format(sPrint2, 27, " ", DataFormat.ALIGN_RIGHT)
                            )
                        )
                        if (!addPrintLine(sPrintLine)) return@Thread

                        sPrint1 = "승인 번호: "
                        sPrint2 = approvedPaymentType.authCode
                        sPrintLine = printLine(
                            listOf(
                                format(sPrint1, 15, " ", DataFormat.ALIGN_LEFT),
                                format(sPrint2, 27, " ", DataFormat.ALIGN_RIGHT)
                            )
                        )
                        if (!addPrintLine(sPrintLine)) return@Thread

                        if (approvedPaymentType.acquirer.isNotEmpty()) {
                            sPrint1 = "카드종류: "
                            sPrint2 = approvedPaymentType.acquirer
                            sPrintLine = printLine(
                                listOf(
                                    format(sPrint1, 15, " ", DataFormat.ALIGN_LEFT),
                                    format(sPrint2, 27, " ", DataFormat.ALIGN_RIGHT)
                                )
                            )
                            if (!addPrintLine(sPrintLine)) return@Thread
                        }

                        if (approvedPaymentType.issuer.isNotEmpty()) {
                            sPrint1 = "카드발급사: "
                            sPrint2 = approvedPaymentType.issuer
                            sPrintLine = printLine(
                                listOf(
                                    format(sPrint1, 15, " ", DataFormat.ALIGN_LEFT),
                                    format(sPrint2, 27, " ", DataFormat.ALIGN_RIGHT)
                                )
                            )
                            if (!addPrintLine(sPrintLine)) return@Thread
                        }

                        if (!addPrintLine(sPrintLine)) return@Thread

                        var maskCardNum = approvedPaymentType.cardNumber.padEnd(16, '*')
                        sPrint1 = "카드 번호: "
                        sPrint2 = "${maskCardNum.substring(0, 4)}-${maskCardNum.substring(4, 8)}-${maskCardNum.substring(8, 12)}-${maskCardNum.substring(12)}"
                        sPrintLine = printLine(
                            listOf(
                                format(sPrint1, 15, " ", DataFormat.ALIGN_LEFT),
                                format(sPrint2, 27, " ", DataFormat.ALIGN_RIGHT)
                            )
                        )
                        if (!addPrintLine(sPrintLine)) return@Thread

                        sPrint1 = "결제 방법: "
                        sPrint2 = if (approvedPaymentType.installment.toInt() == 0) "일시불" else "${approvedPaymentType.installment} 개월"
                        sPrintLine = printLine(
                            listOf(
                                format(sPrint1, 15, " ", DataFormat.ALIGN_LEFT),
                                format(sPrint2, 27, " ", DataFormat.ALIGN_RIGHT)
                            )
                        )
                        if (!addPrintLine(sPrintLine)) return@Thread

                        if (!addPrintLine("==========================================")) return@Thread

                        sPrint1 = "공 급 가: "
                        sPrint2 = (if (approvedPaymentType.purchaseType == PurchaseType.REFUND) "- " else "") + approvedPaymentType.getSupplyAmount() + " 원"
                        sPrintLine = printLine(
                            listOf(
                                format(sPrint1, 15, " ", DataFormat.ALIGN_LEFT),
                                format(sPrint2, 27, " ", DataFormat.ALIGN_RIGHT)
                            )
                        )
                        if (!addPrintLine(sPrintLine)) return@Thread

                        sPrint1 = "부 가 세: "
                        sPrint2 = (if (approvedPaymentType.purchaseType == PurchaseType.REFUND) "- " else "") + approvedPaymentType.getTaxAmount() + " 원"
                        sPrintLine = printLine(
                            listOf(
                                format(sPrint1, 15, " ", DataFormat.ALIGN_LEFT),
                                format(sPrint2, 27, " ", DataFormat.ALIGN_RIGHT)
                            )
                        )
                        if (!addPrintLine(sPrintLine)) return@Thread

                        if (approvedPaymentType.serviceAmount.toInt() > 0) {
                            sPrint1 = "봉 사 료: "
                            sPrint2 = (if (approvedPaymentType.purchaseType == PurchaseType.REFUND) "- " else "") + approvedPaymentType.serviceAmount + " 원"
                            sPrintLine = printLine(
                                listOf(
                                    format(sPrint1, 15, " ", DataFormat.ALIGN_LEFT),
                                    format(sPrint2, 27, " ", DataFormat.ALIGN_RIGHT)
                                )
                            )
                            if (!addPrintLine(sPrintLine)) return@Thread
                        }

                        sPrint1 = "승인 금액: "
                        sPrint2 = (if (approvedPaymentType.purchaseType == PurchaseType.REFUND) "- " else "") + approvedPaymentType.totalAmount + " 원"
                        sPrintLine = printLine(
                            listOf(
                                format(sPrint1, 15, " ", DataFormat.ALIGN_LEFT),
                                format(sPrint2, 27, " ", DataFormat.ALIGN_RIGHT)
                            )
                        )
                    }
                    is ApprovedPaymentType.PaymentHistoryViewInfo -> TODO()
                }


                if (!addPrintLine(sPrintLine)) return@Thread
                if (!addPrintLine("==========================================")) return@Thread
                if (!addNewLine(3)) return@Thread

                printTextToCurCache()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun addNewLine(nNewLineCnt: Int): Boolean {
        for (i in 0 until nNewLineCnt) {
            if (!addPrintLine(" ")) {
                return false
            }
        }
        return true
    }

    private fun addPrintLine(sPrintLine: String): Boolean {
        posPrinter.addTextToCurCache(sPrintLine)

        var nStringLength = 0
        var nLineCnt = 0
        val found = "\n"
        var startPos = 0
        var num = sPrintLine.indexOf(found)
        // new line이 없는 경우
        if (num == -1) {
            num = sPrintLine.length
        }

        while (num >= 0) {
            val strPart = sPrintLine.substring(startPos, num)
            try {
                nStringLength = strPart.toByteArray(charset("euc-kr")).size
            } catch (e: Exception) {
                // Exception handling if necessary
            }

            if (nStringLength <= printerInfo.colPerLine) {
                nLineCnt++
            } else {
                nLineCnt += (nStringLength / printerInfo.colPerLine) + 1
            }

            startPos = num + 1
            num = sPrintLine.indexOf(found, startPos)
            if (num == -1 && startPos < sPrintLine.length) {  // 마지막 조각 처리
                num = sPrintLine.length
            }
        }

        printerInfo.printLineCount += nLineCnt
        // LOG.d(TAG, "${oPrinterInfo.printLineCount} - $sPrintLine")

        // 한 번에 출력할 수 있는 기준보다 출력하려는 라인이 많은 경우 나눠서 출력하기 위해서
        if (maxLine <= printerInfo.printLineCount) {
            printerInfo.printLineCount = 0
            return printTextToCurCache()
        }

        return true
    }

    fun printTextToCurCache(): Boolean {
        printerInfo.isPrintStatus = false
        posPrinter.print()

        return synchronizedObjectWait(printerInfo)
    }

    private fun synchronizedObjectWait(
        printerInfo: PrinterInfo
    ): Boolean {
        var retCode = false
        synchronized(printObject) {
            try {
                printObject.wait()
                retCode = printerInfo.isPrintStatus
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        return retCode
    }

    fun printLine(strs: List<String>): String {
        var strRet = "";
        strs.forEach {
            strRet += it
        }
        return strRet;
    }

    fun format(paramString: String, length: Int, fillText: String, align: DataFormat): String {
        val pad = fillText.substring(0, 1)
        val pattern = pad.repeat(length)

        val paramStringLength = try {
            paramString.toByteArray(charset("euc-kr")).size
        } catch (e: Exception) {
            0
        }

        if (length <= paramStringLength) {
            return paramString
        }

        return when (align) {
            DataFormat.ALIGN_LEFT -> paramString + pattern.substring(0, length - paramStringLength)
            DataFormat.ALIGN_RIGHT -> pattern.substring(0, length - paramStringLength) + paramString
            DataFormat.ALIGN_CENTER -> {
                if (paramString.length > pattern.length) {
                    paramString
                } else {
                    val leftMargin = (length - paramString.length) / 2
                    val rightMargin = if ((leftMargin * 2) == (length - paramString.length)) leftMargin else leftMargin + 1
                    pattern.substring(0, leftMargin) + paramString + pattern.substring(pattern.length - rightMargin)
                }
            }
            else -> throw IllegalArgumentException("#align is invalid")
        }
    }
}