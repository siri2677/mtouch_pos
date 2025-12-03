package com.kwonps.domain.service.payment

import com.kwonps.domain.model.payment.AmountData
import com.kwonps.domain.model.payment.PaymentError

object PaymentValidator {
    fun validateAmount(amount: AmountData): PaymentError.Validation? {
        if (amount.totalAmount <= 0) {
            return PaymentError.Validation("결제 금액은 0보다 커야 합니다.")
        }

        if (amount.freeAmount < 0 || amount.serviceAmount < 0) {
            return PaymentError.Validation("면세/봉사료는 음수가 될 수 없습니다.")
        }

        if (amount.freeAmount + amount.serviceAmount > amount.totalAmount) {
            return PaymentError.Validation("면세 혹은 봉사료가 총 금액을 초과할 수 없습니다.")
        }

        return null
    }
}
