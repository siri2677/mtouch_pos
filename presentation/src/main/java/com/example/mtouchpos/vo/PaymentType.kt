package com.example.mtouchpos.vo

enum class PaymentType(val code: String, val status: String) {
    Approve("0200", "결제승인"), Refund("0420", "취소승인")
}