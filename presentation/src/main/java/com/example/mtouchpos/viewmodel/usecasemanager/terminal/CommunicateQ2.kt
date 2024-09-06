package com.example.mtouchpos.viewmodel.usecasemanager.terminal

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.PaymentVanData
import com.example.domain.model.payment.RootPaymentData
import com.example.domain.usecase.device.manager.CommunicateCardTerminalManager

class CommunicateQ2(
    private val context: Context,
): CommunicateCardTerminalManager {
    override fun invoke(
        paymentInfo: OfflinePaymentData,
        paymentVanInfo: PaymentVanData,
        rootPaymentInfo: RootPaymentData?
    ): String? {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(""))
            context.startActivity(intent)
            null
        } catch (e: ActivityNotFoundException) {
            "앱 설치 후 결제 요청바랍니다"
        }
    }
}