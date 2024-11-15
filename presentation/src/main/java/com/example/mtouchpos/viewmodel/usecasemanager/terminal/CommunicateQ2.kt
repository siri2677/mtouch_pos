package com.example.mtouchpos.viewmodel.usecasemanager.terminal

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.VanData
import com.example.domain.usecase.device.manager.CommunicateCardTerminalManager
import com.example.mtouchpos.viewmodel.factory.CardTerminalFactory.CallBack
import java.io.Serializable

class CommunicateQ2(
    private val context: Context,
    private val callBack: CallBack
) : CommunicateCardTerminalManager, Serializable {
    override fun invoke(
        paymentInfo: OfflinePaymentData,
        paymentVanInfo: VanData
    ): String? = try {
        SchipQ2(
            trmnlNo = paymentVanInfo.dptId,
            delngSe = when (paymentInfo) {
                is OfflinePaymentData.Approve -> "1"
                is OfflinePaymentData.Cancel -> "0"
            },
            splpc = paymentInfo.amountData.supplyAmount.toString(),
            vat = paymentInfo.amountData.vat.toString(),
            taxxpt = paymentInfo.amountData.freeAmount.toString(),
            callbackAppUrl = callBack.url,
            srcConfmNo = if (paymentInfo is OfflinePaymentData.Cancel) paymentInfo.authCode else "",
            srcConfmDe = if (paymentInfo is OfflinePaymentData.Cancel) paymentInfo.authDate.substring(2, 8) else "",
            srcInstlmtMonth = if (paymentInfo is OfflinePaymentData.Cancel) paymentInfo.installment else "",
        ).toString().let {
            Log.w("Q2", it)
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
        }
        null
    } catch (e: ActivityNotFoundException) {
        "앱 설치 후 결제 요청바랍니다"
    }

    data class SchipQ2(
        private val bizrno: String = "4198800046",
        private val cardCashSe: String = "CARD",
        private val trmnlNo: String,
        private val delngSe: String,
        private val splpc: String,
        private val vat: String,
        private val taxxpt: String,
        private val srcConfmNo: String,
        private val srcConfmDe: String,
        private val srcInstlmtMonth: String,
        private val limitInstMonth: String = "00",
        private val isPG: String = "O",
        private val callbackAppUrl: String,
        private val scheme: String = "fpispksnet://default"
    ) : Serializable {
        override fun toString() = Uri.Builder()
            .scheme(scheme)
            .appendQueryParameter("bizrno", bizrno)
            .appendQueryParameter("trmnlNo", trmnlNo)
            .appendQueryParameter("cardCashSe", cardCashSe)
            .appendQueryParameter("delngSe", delngSe)
            .appendQueryParameter("splpc", splpc)
            .appendQueryParameter("vat", vat)
            .appendQueryParameter("taxxpt", taxxpt)
            .appendQueryParameter("limitInstMonth", limitInstMonth)
            .appendQueryParameter("callbackAppUrl", callbackAppUrl)
            .appendQueryParameter("isPG", isPG)
            .appendQueryParameter("srcConfmNo", srcConfmNo)
            .appendQueryParameter("srcConfmDe", srcConfmDe)
            .appendQueryParameter("srcInstlmtMonth", srcInstlmtMonth)
            .build().toString()
    }
}