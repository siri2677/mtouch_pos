package com.kwonps.domain.model.payment

data class AmountData(
    val totalAmount: Int,
    val freeAmount: Int = 0,
    val serviceAmount: Int = 0
) {
    fun getSupplyAmount() = totalAmount - freeAmount - serviceAmount - ((totalAmount - freeAmount - serviceAmount) / 11)
    fun getVat() = (totalAmount - freeAmount - serviceAmount) / 11
}