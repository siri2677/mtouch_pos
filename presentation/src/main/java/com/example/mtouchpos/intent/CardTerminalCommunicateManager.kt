package com.example.mtouchpos.intent

import com.example.domain.model.payment.OfflinePaymentData

interface CardTerminalCommunicateManager {
    operator fun invoke(
        paymentInfo: OfflinePaymentData,
        dptId: String
    ): String?
}