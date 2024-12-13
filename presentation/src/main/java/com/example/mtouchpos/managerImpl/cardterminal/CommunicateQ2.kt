package com.example.mtouchpos.managerImpl.cardterminal

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.VanData
import com.example.domain.manager.cardterminal.CardTerminalCommunicateManager
import com.example.mtouchpos.managerImpl.factory.CardTerminalFactory
import java.io.Serializable

class CommunicateQ2(
    private val context: Context,
    private val callBack: CardTerminalFactory.CallBack
) : CardTerminalCommunicateManager, Serializable {
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
            srcConfmDe = if (paymentInfo is OfflinePaymentData.Cancel) paymentInfo.authDate else "",
            srcInstlmtMonth = if (paymentInfo is OfflinePaymentData.Cancel) paymentInfo.installment else "",
        ).toString().let {
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