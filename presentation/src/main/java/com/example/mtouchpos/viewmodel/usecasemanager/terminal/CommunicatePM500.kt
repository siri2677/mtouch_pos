package com.example.mtouchpos.viewmodel.usecasemanager.terminal

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.VanData
import com.example.domain.usecase.device.manager.CommunicateCardTerminalManager
import java.io.Serializable


class CommunicatePM500(
    private val launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
): CommunicateCardTerminalManager, Serializable {
    override fun invoke(
        paymentInfo: OfflinePaymentData,
        paymentVanInfo: VanData
    ): String? = try {
        PmPos(
            trdtype = when (paymentInfo) {
                is OfflinePaymentData.Approve -> "F1"
                is OfflinePaymentData.Cancel -> "F2"
            },
            amount = paymentInfo.amountData.totalAmount.toString(),
            fee = paymentInfo.amountData.serviceAmount.toString(),
            surtax = paymentInfo.amountData.vat.toString(),
            tax_free = paymentInfo.amountData.freeAmount.toString(),
            installment = paymentInfo.installment,
            org_approval_date = if (paymentInfo is OfflinePaymentData.Cancel) paymentInfo.authDate else "",
            org_approval_no = if (paymentInfo is OfflinePaymentData.Cancel) paymentInfo.authCode else "",
            catId = paymentVanInfo.dptId
        ).toString().let {
            launcher.launch(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
        }
        null
    } catch (e: ActivityNotFoundException) {
        "앱 설치 후 결제 요청바랍니다"
    }

    data class PmPos(
        private val trdtype: String,
        private val amount: String,
        private val fee: String,
        private val surtax: String,
        private val tax_free: String,
        private val installment: String,
        private val org_approval_date: String,
        private val org_approval_no: String,
        private val catId: String,
        private val business_no: String = "4198800046",
        private val scheme: String = "pmposapp://pointmobile"
    ) : Serializable {
        override fun toString() = Uri.Builder()
            .scheme(scheme)
            .appendQueryParameter("trdtype", trdtype)
            .appendQueryParameter("amount", amount)
            .appendQueryParameter("fee", fee)
            .appendQueryParameter("surtax", surtax)
            .appendQueryParameter("installment", installment)
            .appendQueryParameter("tax_free", tax_free)
            .appendQueryParameter("org_approval_date", org_approval_date)
            .appendQueryParameter("org_approval_no", org_approval_no)
            .appendQueryParameter("catid", catId)
            .appendQueryParameter("business_no", business_no)
            .build().toString()
    }
}