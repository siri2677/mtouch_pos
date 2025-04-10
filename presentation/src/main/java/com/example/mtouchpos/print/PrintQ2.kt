package com.example.mtouchpos.print

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.cloudpos.DeviceException
import com.cloudpos.POSTerminal
import com.cloudpos.jniinterface.PrinterInterface
import com.cloudpos.printer.PrinterDevice
import com.cloudpos.printer.PrinterHtmlListener
import com.cloudpos.printer.PrinterHtmlListener.PRINT_SUCCESS
import com.cloudpos.sdk.printer.html.PrinterHtmlUtil
import com.example.domain.model.payment.OfflinePaymentData
import com.example.mtouchpos.vo.info.ApprovedPaymentType
import com.example.mtouchpos.vo.info.UserInfo
import com.example.mtouchpos.vo.type.PurchaseType
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.io.Serializable

class PrintQ2(
    private val context: Context,
    private val userInfo: UserInfo,
    private val approvedPaymentType: ApprovedPaymentType
) : CardTerminalPrintManager, Serializable {
    val device: PrinterDevice = POSTerminal.getInstance(context).getDevice("cloudpos.device.printer") as PrinterDevice
    val printerHtmlListener = object : PrinterHtmlListener {
        override fun onGet(bitmap: Bitmap, errorCode: Int) {
        }

        override fun onFinishPrinting(errorCode: Int) {
        }
    }

    override fun invoke() {
        var htmlText = "<html>${setHead()}<body id='mybody'>"

        htmlText += drawDashLine()
        htmlText += "<span style = \"font-size:1.75em;\">"
        htmlText += "가 맹 점 명: ${userInfo.mchtName}<br>"
        htmlText += "대 표 자 명: ${userInfo.ceoName}<br>"
        htmlText += "사업자 번호: ${userInfo.identity}<br>"
        htmlText += "전 화 번 호: ${userInfo.telNo}<br>"
        userInfo.address.takeIf { it.isNotEmpty() }?.let { htmlText += "주       소: ${it.replace("+", " ")}<br>" }
        htmlText += "</span>"

        htmlText += drawDashLine()
        approvedPaymentType.let {
            when(it) {
                is ApprovedPaymentType.CompletePaymentViewInfo -> {
                    htmlText += setTitle("** 신용${if (it.purchaseType == PurchaseType.APPROVE) "승인" else "취소"} 정보 **")
                    htmlText += "<span style = \"font-size:1.75em;\">"
                    htmlText += "거래  일시: ${it.authDate}<br>"
                    htmlText += "승인  번호: ${it.authCode}<br>"
                    htmlText += "카드  종류: ${it.issuer}<br>"
                    htmlText += "카드발급사: ${it.acquirer}<br>"
                    htmlText += "카드  번호: ${it.cardNumber}<br>"
                    htmlText += "결제  방법: ${if(it.installment == "0") "일시불" else "${it.installment} 개월"}<br>"
                    htmlText += "</span>"

                    htmlText += drawEqualLine()
                    htmlText += addTableHeader()
                    htmlText += addTableColumn("공  급  가:", "${if(it.purchaseType == PurchaseType.APPROVE) "" else "-"}${it.getSupplyAmount()} 원")
                    htmlText += addTableColumn("부  가  세:", "${if(it.purchaseType == PurchaseType.APPROVE) "" else "-"}${it.getTaxAmount()} 원")
                    if (it.serviceAmount.toInt() > 0)
                        htmlText += addTableColumn("봉  사  료:", "${if(it.purchaseType == PurchaseType.APPROVE) "" else "-"}${it.serviceAmount} 원")
                    htmlText += addTableColumn("승인  금액:", "${if(it.purchaseType == PurchaseType.APPROVE) "" else "-"}${it.totalAmount} 원")
                }
                is ApprovedPaymentType.PaymentHistoryViewInfo -> TODO()
            }
        }

        htmlText += addTableTail()
        htmlText += drawEqualLine()
        htmlText += addTableHeader()
        htmlText += addTableTail()
        htmlText += "<br><br><br><br></body></html>"

        try {
            device.open()
            device.printHTML(htmlText, printerHtmlListener)
        } catch (e: DeviceException) {
            device.printHTML(htmlText, printerHtmlListener)
        }
    }

    fun drawDashLine(): String =
        "<span style = \"font-size:1.2em;\">----------------------------------<br></span>"

    fun drawEqualLine(): String =
        "<span style = \"font-size:1.2em;\">======================================<br></span>"

    fun setHead(): String =
        """
        <head>
            <title>local webview</title>
            <meta name='format-detection' content='telephone=no'/>
            <meta name="viewport" content="width=device-width, user-scalable=no" />
            <style type="text/css">
                @font-face { font-family: 'testFont'; src: url('file:///android_asset/sdwwagger.ttf'); }
                body {font-family: 'testFont';}
            </style>
        </head>
        """.trimIndent()

    fun setTitle(title: String): String =
        """
        <span style = "font-size:1.8em;">
            <center><u>$title<br></u></center>
        </span><br>
        """.trimIndent()

    fun addTableHeader(): String =
        """
        <style>
            table {border-collapse: collapse;}
        </style>
        <table width="376">
        """.trimIndent()

    fun addTableTail(): String = "</table>"

    fun addTableColumn(title: String, contents: String): String =
        """
        <tr>
            <td width="110" style = "font-size:1.75em;">$title</td>
            <td width="230" align="right" style = "font-size:1.75em;">$contents</td>
        </tr>
        """.trimIndent()
}