package com.example.domain.usecase.device.manager

import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.VanData
import java.io.Serializable

interface CommunicateCardTerminalManager: Serializable {
    operator fun invoke(
        paymentInfo: OfflinePaymentData,
        paymentVanInfo: VanData
    ): String?
}