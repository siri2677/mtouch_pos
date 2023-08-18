package com.example.domain.enumclass

enum class KsnetParsingByte(val value: Int) {
    IDX_COMMAND(3),
    IDX_DATA(4);

    fun keyValue(): Int {
        return value
    }
}
