package com.kwonps.domain.service.payment

/**
 * Domain-level calculator for payment amounts and VAT/supply breakdowns.
 * Keeps business rules close to the model instead of spreading raw math across layers.
 */
object PaymentAmountCalculator {
    private const val VAT_DIVISOR = 11
    private const val SIGN_TRAN_THRESHOLD = 50_000

    fun supplyAmount(totalAmount: Int, freeAmount: Int = 0, serviceAmount: Int = 0): Int {
        val taxable = taxableAmount(totalAmount, freeAmount, serviceAmount)
        return taxable - vat(taxable)
    }

    fun vatAmount(totalAmount: Int, freeAmount: Int = 0, serviceAmount: Int = 0): Int {
        val taxable = taxableAmount(totalAmount, freeAmount, serviceAmount)
        return vat(taxable)
    }

    fun signTranFlag(totalAmount: Int): String = if (totalAmount > SIGN_TRAN_THRESHOLD) "S" else "N"

    private fun taxableAmount(totalAmount: Int, freeAmount: Int, serviceAmount: Int): Int =
        totalAmount - freeAmount - serviceAmount

    private fun vat(taxableAmount: Int): Int = taxableAmount / VAT_DIVISOR
}
