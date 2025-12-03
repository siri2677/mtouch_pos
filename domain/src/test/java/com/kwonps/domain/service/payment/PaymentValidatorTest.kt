package com.kwonps.domain.service.payment

import com.kwonps.domain.model.payment.AmountData
import com.kwonps.domain.model.payment.PaymentError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaymentValidatorTest {

    @Test
    fun `accepts valid amounts`() {
        val valid = AmountData(totalAmount = 10_000, freeAmount = 1_000, serviceAmount = 500)

        val result = PaymentValidator.validateAmount(valid)

        assertNull(result)
    }

    @Test
    fun `rejects zero or negative total amounts`() {
        val invalid = AmountData(totalAmount = 0, freeAmount = 0, serviceAmount = 0)

        val result = PaymentValidator.validateAmount(invalid)

        assertEquals(PaymentError.Validation("결제 금액은 0보다 커야 합니다."), result)
    }

    @Test
    fun `rejects negative surcharges`() {
        val invalid = AmountData(totalAmount = 10_000, freeAmount = -1, serviceAmount = 0)

        val result = PaymentValidator.validateAmount(invalid)

        assertEquals(PaymentError.Validation("면세/봉사료는 음수가 될 수 없습니다."), result)
    }

    @Test
    fun `rejects surcharges exceeding total`() {
        val invalid = AmountData(totalAmount = 10_000, freeAmount = 6_000, serviceAmount = 5_000)

        val result = PaymentValidator.validateAmount(invalid)

        assertEquals(PaymentError.Validation("면세 혹은 봉사료가 총 금액을 초과할 수 없습니다."), result)
    }
}
