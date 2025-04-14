package com.kwonps.domain.model.paymentHistory

data class DailyAndMonthlyPaymentStatisticData(
    val today: PaymentStatisticData,
    val month: PaymentStatisticData
)