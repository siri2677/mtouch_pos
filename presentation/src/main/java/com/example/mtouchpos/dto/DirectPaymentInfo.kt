package com.example.mtouchpos.dto

data class DirectPaymentInfo(
    val productName: String,
    val cardNumber: String,
    val expiry: String,
    val cardAuth: String,
    val payerName: String,
    val payerTel: String,
    val authPw: String?,
    val authDob: String?,
)
//{
//    init {
//        require(payerName.isNotBlank()) {"구매자명을\n 입력해주시기 바랍니다"}
//        require(cardNumber.isNotBlank()) {"카드 번호를\n 입력해주시기 바랍니다"}
//        require(amount.isNotBlank()) {"결제 금액을\n 입력해주시기 바랍니다"}
//        require(payerTel.isNotBlank()) {"구매자 연락처를\n 입력해주시기 바랍니다"}
//        require(productName.isNotBlank()) {"상품명을\n 입력해주시기 바랍니다"}
//        if(cardAuth == "true") {
//            require(authPw!!.isNotBlank()) {"비밀번호 앞 2자리를\n 입력해주시기 바랍니다"}
//            require(authDob!!.isNotBlank()) {"생년월일을 입력해주세요."}
//        }
//    }
//}