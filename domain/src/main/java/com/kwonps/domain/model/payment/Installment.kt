package com.kwonps.domain.model.payment

data class Installment(val value: String) {
    init {
        require(value.isNotBlank()) { "Installment must not be blank" }
    }

    val isLumpSum: Boolean = value == "00" || value == "0"
}
