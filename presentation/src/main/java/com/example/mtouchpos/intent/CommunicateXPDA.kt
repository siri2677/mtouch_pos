package com.example.mtouchpos.intent

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.domain.model.payment.OfflinePaymentData
import java.io.Serializable
import kotlin.String
import kotlin.let

class CommunicateXPDA(
    private val context: Context,
    private val callBack: CardTerminalFactory.CallBack
): CardTerminalCommunicateManager, Serializable {
    override fun invoke(
        paymentInfo: OfflinePaymentData,
        dptId: String
    ): String? = try {
        ITWellScheme(
            tid = dptId,
            inputAmount = paymentInfo.amountData.totalAmount.toString(),
            inputMonth = paymentInfo.installment,
            returnUrl = callBack.url,
            inputApprovalNo = if (paymentInfo is OfflinePaymentData.Cancel) paymentInfo.authCode else "",
            inputApprovalDate = if (paymentInfo is OfflinePaymentData.Cancel) "20" + paymentInfo.authDate else "",
            scheme = when(paymentInfo) {
                is OfflinePaymentData.Approve -> "itwellksnet://card-approval"
                is OfflinePaymentData.Cancel -> "itwellksnet://card-revoke"
            }
        ).toString().let {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
        }
        null
    } catch (e: ActivityNotFoundException) {
        "앱 설치 후 결제 요청바랍니다"
    }

    data class ITWellScheme(
        private val tid: String,
        private val regNo: String = "1208197322",
        private val runMode: String = "scheme",
        private val runStrategy: String = "without-input-view",
        private val showResult: String = "false",
        private val saveTran: String = "false",
        private val autoDownload: String = "true",
        private val inputAmountEditable: String = "false",
        private val inputMonthEditable: String = "false",
        private val inputApprovalNoEditable: String = "false",
        private val inputApprovalDateEditable: String = "false",
        private val inputAmount: String,
        private val inputMonth: String,
        private val inputApprovalNo: String,
        private val inputApprovalDate: String,
        private val configVat: String = "10",
        private val configVatAdd: String = "0",
        private val configService: String = "0",
        private val configServiceAdd: String = "0",
        private val returnUrl: String,
        private val scheme: String
    ) : Serializable {
        override fun toString() = Uri.Builder()
            .scheme(scheme)
            .appendQueryParameter("tid", tid)
            .appendQueryParameter("regNo", regNo)
            .appendQueryParameter("runMode", runMode)
            .appendQueryParameter("runStrategy", runStrategy)
            .appendQueryParameter("showResult", showResult)
            .appendQueryParameter("saveTran", saveTran)
            .appendQueryParameter("autoDownload", autoDownload)
            .appendQueryParameter("inputAmountEditable", inputAmountEditable)
            .appendQueryParameter("inputMonthEditable", inputMonthEditable)
            .appendQueryParameter("inputApprovalNoEditable", inputApprovalNoEditable)
            .appendQueryParameter("inputApprovalDateEditable", inputApprovalDateEditable)
            .appendQueryParameter("inputAmount", inputAmount)
            .appendQueryParameter("inputMonth", inputMonth)
            .appendQueryParameter("inputApprovalNo", inputApprovalNo)
            .appendQueryParameter("inputApprovalDate", inputApprovalDate)
            .appendQueryParameter("configVat", configVat)
            .appendQueryParameter("configVatAdd", configVatAdd)
            .appendQueryParameter("configService", configService)
            .appendQueryParameter("configServiceAdd", configServiceAdd)
            .appendQueryParameter("returnUrl", returnUrl)
            .build().toString()
    }
}