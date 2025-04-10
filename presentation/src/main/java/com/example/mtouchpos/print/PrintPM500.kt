package com.example.mtouchpos.print

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.example.domain.model.payment.OfflinePaymentData
import com.example.mtouchpos.vo.info.ApprovedPaymentType
import com.example.mtouchpos.vo.info.UserInfo
import com.example.mtouchpos.vo.type.PurchaseType
import device.sdk.print.Printer
import device.sdk.print.ReceiptPrint
import device.sdk.print.wrapper.IPrint
import kotlinx.coroutines.flow.channelFlow
import vpos.apipackage.Print
import java.io.Serializable


class PrintPM500(
    private val context: Context,
    private val userInfo: UserInfo,
    private val approvedPaymentType: ApprovedPaymentType
): CardTerminalPrintManager, Serializable {
    override operator fun invoke() {
        Thread {
            getPrinter().print(makeCashReceiptPrint())
        }.start()
    }

    fun getPrinter(): Printer = Printer.open(object : IPrint {
        override fun init(): Int {
            return Print.Lib_PrnInit()
        }

        override fun print(): Int {
            return Print.Lib_PrnStart()
        }

        override fun checkStatus(): Int {
            return Print.Lib_PrnCheckStatus()
        }

        override fun setGray(b: Byte): Int {
            return Print.Lib_PrnSetGray(b)
        }

        override fun addBmp(array: ByteArray): Int {
            return Print.Lib_PrnLogo(array)
        }
    }).also {
        it.setGrayValue(4)
    }

    fun makeCashReceiptPrint(): ReceiptPrint {
        val receiptPrint = ReceiptPrint(context)
        val param = receiptPrint.param
        try {
            param.textSize = 23f
            receiptPrint.param = param

            receiptPrint.addTextLine("------------------------------")
            receiptPrint.addTextAlign("가 맹 점 명 : ${userInfo.mchtName}", ReceiptPrint.ALIGN_LEFT)
            receiptPrint.addText("\n")
            receiptPrint.addTextAlign("대 표 자 명 : ", ReceiptPrint.ALIGN_LEFT)
            receiptPrint.addTextAlign(userInfo.ceoName, ReceiptPrint.ALIGN_RIGHT)
            receiptPrint.addText("\n")
            receiptPrint.addTextAlign("사업자 번호 : ", ReceiptPrint.ALIGN_LEFT)
            receiptPrint.addTextAlign(userInfo.identity, ReceiptPrint.ALIGN_RIGHT)
            receiptPrint.addText("\n")
            receiptPrint.addTextAlign("전 화 번 호 : ", ReceiptPrint.ALIGN_LEFT)
            receiptPrint.addTextAlign(userInfo.telNo, ReceiptPrint.ALIGN_RIGHT)
            receiptPrint.addText("\n")
            receiptPrint.addTextAlign("주       소 : ${userInfo.address}", ReceiptPrint.ALIGN_LEFT)
            receiptPrint.addText("\n")
            receiptPrint.addTextLine("------------------------------")

            param.textSize = 30f
            receiptPrint.param = param

            when(approvedPaymentType) {
                is ApprovedPaymentType.CompletePaymentViewInfo -> {
                    receiptPrint.addTextAlign("** 신용${if(approvedPaymentType.purchaseType == PurchaseType.APPROVE) "승인" else "취소"}정보 **", ReceiptPrint.ALIGN_CENTER)
                    receiptPrint.addText("\n\n")

                    param.textSize = 23f
                    receiptPrint.param = param

                    receiptPrint.addTextAlign("거래 일시 : ", ReceiptPrint.ALIGN_LEFT)
                    receiptPrint.addTextAlign(approvedPaymentType.authDate, ReceiptPrint.ALIGN_RIGHT)
                    receiptPrint.addText("\n")
                    receiptPrint.addTextAlign("승인 번호 : ", ReceiptPrint.ALIGN_LEFT)
                    receiptPrint.addTextAlign(approvedPaymentType.authCode, ReceiptPrint.ALIGN_RIGHT)
                    receiptPrint.addText("\n")
                    if (approvedPaymentType.acquirer.isNotEmpty()) {
                        receiptPrint.addTextAlign("전표매입사 : ", ReceiptPrint.ALIGN_LEFT)
                        receiptPrint.addTextAlign(approvedPaymentType.acquirer, ReceiptPrint.ALIGN_RIGHT)
                        receiptPrint.addText("\n")
                    }
                    if (approvedPaymentType.issuer.isNotEmpty()) {
                        receiptPrint.addTextAlign("카드발급사 : ", ReceiptPrint.ALIGN_LEFT)
                        receiptPrint.addTextAlign(approvedPaymentType.issuer, ReceiptPrint.ALIGN_RIGHT)
                        receiptPrint.addText("\n")
                    }
                    receiptPrint.addTextAlign("카드 번호 : ", ReceiptPrint.ALIGN_LEFT)
                    receiptPrint.addTextAlign(approvedPaymentType.cardNumber, ReceiptPrint.ALIGN_RIGHT)
                    receiptPrint.addText("\n")
                    receiptPrint.addTextAlign("결제 방법 : ", ReceiptPrint.ALIGN_LEFT)
                    receiptPrint.addTextAlign(if(approvedPaymentType.installment.toInt() == 0) "일시불" else "${approvedPaymentType.installment} 개월", ReceiptPrint.ALIGN_RIGHT)
                    receiptPrint.addText("\n")
                    receiptPrint.addTextLine("=============================")
                    receiptPrint.addTextAlign("공 급 가 : ", ReceiptPrint.ALIGN_LEFT)
                    receiptPrint.addTextAlign((if(approvedPaymentType.purchaseType == PurchaseType.APPROVE) "" else "-") + "${approvedPaymentType.getSupplyAmount()} 원", ReceiptPrint.ALIGN_RIGHT)
                    receiptPrint.addText("\n")

                    if (approvedPaymentType.getTaxAmount().toInt() != 0) {
                        receiptPrint.addTextAlign("부 가 세 : ", ReceiptPrint.ALIGN_LEFT)
                        receiptPrint.addTextAlign((if(approvedPaymentType.purchaseType == PurchaseType.APPROVE) "" else "-") + "${approvedPaymentType.getTaxAmount()} 원", ReceiptPrint.ALIGN_RIGHT)
                        receiptPrint.addText("\n")
                    }
                    if (approvedPaymentType.serviceAmount.toInt() > 0) {
                        receiptPrint.addTextAlign("봉 사 료 : ", ReceiptPrint.ALIGN_LEFT)
                        receiptPrint.addTextAlign((if(approvedPaymentType.purchaseType == PurchaseType.APPROVE) "" else "-") + "${approvedPaymentType.serviceAmount} 원", ReceiptPrint.ALIGN_RIGHT)
                        receiptPrint.addText("\n")
                    }
                    receiptPrint.addTextLine("------------------------------")
                    param.textSize = 27f
                    receiptPrint.param = param
                    receiptPrint.addTextAlign("승인 금액 : ", ReceiptPrint.ALIGN_LEFT)
                    receiptPrint.addTextAlign((if(approvedPaymentType.purchaseType == PurchaseType.APPROVE) "" else "-") + "${approvedPaymentType.totalAmount} 원", ReceiptPrint.ALIGN_RIGHT)
                    receiptPrint.addText("\n")
                    param.textSize = 23f
                    receiptPrint.param = param
                    receiptPrint.addTextLine("=============================")
                }
                is ApprovedPaymentType.PaymentHistoryViewInfo -> TODO()
            }
        } catch (e: ReceiptPrint.BitmapOutOfRangeException) {
            e.printStackTrace()
        }

        return receiptPrint
    }

}