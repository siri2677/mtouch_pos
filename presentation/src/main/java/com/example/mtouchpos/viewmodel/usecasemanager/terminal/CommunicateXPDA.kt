package com.example.mtouchpos.viewmodel.usecasemanager.terminal

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.VanData
import com.example.domain.usecase.device.manager.CommunicateCardTerminalManager
import java.io.Serializable

class CommunicateXPDA(
    private val context: Context,
    private val launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
): CommunicateCardTerminalManager, Serializable {
    override fun invoke(
        paymentInfo: OfflinePaymentData,
        paymentVanInfo: VanData
    ): String? {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(""))
            launcher.launch(intent)
            null
        } catch (e: ActivityNotFoundException) {
            "앱 설치 후 결제 요청바랍니다"
        }
    }
}