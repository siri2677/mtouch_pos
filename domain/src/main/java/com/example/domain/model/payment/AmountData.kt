package com.example.domain.model.payment

data class AmountData(
    val totalAmount: Int,
    val freeAmount: Int = 0, // 면세 가맹점 totalAmount - serviceAmount
    val serviceAmount: Int = 0,
    private val taxableAmount: Int = totalAmount - freeAmount - serviceAmount,
    val vat: Int = taxableAmount / 11,
    val supplyAmount: Int = totalAmount - serviceAmount - vat
)