package com.kwonps.domain.model.payment

import com.kwonps.domain.service.payment.PaymentAmountCalculator

data class AmountData(
    val totalAmount: Int,
    val freeAmount: Int = 0,
    val serviceAmount: Int = 0
) {
    val supplyAmount: Int = PaymentAmountCalculator.supplyAmount(totalAmount, freeAmount, serviceAmount)
    val vatAmount: Int = PaymentAmountCalculator.vatAmount(totalAmount, freeAmount, serviceAmount)
}
