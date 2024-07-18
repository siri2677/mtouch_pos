package com.example.domain.usecase.device.manager

import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.PaymentVanData
import com.example.domain.model.payment.RootPaymentData

interface CommunicateCardTerminalManager {
    operator fun invoke(
        paymentInfo: OfflinePaymentData,
        paymentVanInfo: PaymentVanData,
        rootPaymentInfo: RootPaymentData?
    ): String?
}