package com.kwonps.mtouchpos.intent

import com.kwonps.domain.model.payment.OfflinePaymentData

interface CardTerminalCommunicateManager {
    operator fun invoke(
        paymentInfo: OfflinePaymentData,
        dptId: String
    ): String?
}