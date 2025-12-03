package com.kwonps.domain.service.payment

import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentAmountCalculatorTest {

    @Test
    fun `calculates vat and supply amounts with defaults`() {
        val total = 11000

        val supply = PaymentAmountCalculator.supplyAmount(total)
        val vat = PaymentAmountCalculator.vatAmount(total)

        assertEquals(10000, supply)
        assertEquals(1000, vat)
    }

    @Test
    fun `excludes free and service amounts from taxable calculation`() {
        val total = 22000
        val free = 2000
        val service = 1000

        val supply = PaymentAmountCalculator.supplyAmount(total, freeAmount = free, serviceAmount = service)
        val vat = PaymentAmountCalculator.vatAmount(total, freeAmount = free, serviceAmount = service)

        assertEquals(17000, supply)
        assertEquals(1700, vat)
    }

    @Test
    fun `sign transaction flag switches after threshold`() {
        assertEquals("N", PaymentAmountCalculator.signTranFlag(50_000))
        assertEquals("S", PaymentAmountCalculator.signTranFlag(50_001))
    }
}
