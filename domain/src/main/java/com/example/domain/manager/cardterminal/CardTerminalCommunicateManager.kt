package com.example.domain.manager.cardterminal

import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.VanData

interface CardTerminalCommunicateManager {
    operator fun invoke(
        paymentInfo: OfflinePaymentData,
        paymentVanInfo: VanData
    ): String?
}