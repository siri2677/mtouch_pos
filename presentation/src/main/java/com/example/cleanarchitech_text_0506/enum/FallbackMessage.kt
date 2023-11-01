package com.example.cleanarchitech_text_0506.enum

enum class FallbackMessage(val value: String) {
    FALLBACK_NO_ATR("01") {
        override fun toString(): String {
            return "Chip 미 응답"
        }
    },
    FALLBACK_NO_APPL("02") {
        override fun toString(): String {
            return "Application 미 존재"
        }
    },
    FALLBACK_READ_FAIL("03"){
        override fun toString(): String {
            return "Chip 데이터 읽기 실패"
        }
    },
    FALLBACK_NO_DATA("04"){
        override fun toString(): String {
            return "Mandatory 데이터 미 포함"
        }
    },
    FALLBACK_CVM_FAIL("05"){
        override fun toString(): String {
            return "CVM 커맨드 응답실패"
        }
    },
    FALLBACK_BAD_CM("06"){
        override fun toString(): String {
            return "EMV 커맨드 오 설정"
        }
    },
    FALLBACK_BAD_OPER("07"){
        override fun toString(): String {
            return "터미널(리더기) 오 동작"
        }
    };

    fun keyValue(): String {
        return value
    }
}