package com.example.mtouchpos.dto

import com.example.mtouchpos.vo.PaymentType
import com.example.mtouchpos.vo.TransactionType
import java.io.Serializable

sealed interface ResponseFlowData : Serializable{
    object Init: ResponseFlowData
    object CompleteLogin : ResponseFlowData

    data class CompletePayment(
        val transactionType: TransactionType,
        val paymentType: PaymentType,
        val amount: String,
        val installment: String,
        val trackId: String,
        val cardNumber: String,
        val authDate: String,
        val authCode: String,
        val trxId: String
    ): ResponseFlowData

    data class CreditPayment (
        val amount: String,
        val authCd: String,
        val trackId: String,
        val bin: String,
        val cardType: String,
        val trxId: String,
        val regDay: String,
        val number: String,
        val trxResult: String,
        val regTime: String,
        val installment: String,
        val brand: String,
        val rfdTime: String?,
        val rfdDay: String?
    ): ResponseFlowData

    data class CreditPaymentList(val list: ArrayList<CreditPayment>): ResponseFlowData

    data class Error(
        val message: String
    ): ResponseFlowData
}