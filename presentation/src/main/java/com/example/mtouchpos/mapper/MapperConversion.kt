package com.example.mtouchpos.mapper

import com.example.mtouchpos.vo.PaymentType
import org.mapstruct.Named
import java.nio.charset.StandardCharsets

@Named("mapperConversion")
class MapperConversion {
    @Named("stringToByteArray")
    fun stringToByteArray(dptId: String) = dptId.toByteArray(StandardCharsets.UTF_8)

    @Named("setSignTran")
    fun setSignTran(amount: Int) = stringToByteArray(if (amount!! > 50000) "S" else "N")

    @Named("intToByteArray")
    fun intToByteArray(value: Int) = stringToByteArray(String.format("%012d", value))

    @Named("setSupplyAmount")
    fun setSupplyAmount(amount: Int) = intToByteArray((amount!! - (amount!! / 11)))

    @Named("setTaxAmount")
    fun setTaxAmount(amount: Int): ByteArray = intToByteArray((amount!! / 11))

    @Named("setTelegramType")
    fun setTelegramType(result: String) = stringToByteArray(if (result == PaymentType.Approve.status) PaymentType.Approve.code else PaymentType.Refund.code)

    @Named("setRegDay")
    fun setRegDay(regDay: String?) = if (regDay == null) null else stringToByteArray(regDay!!.substring(2, 8))

    @Named("setAuthCode")
    fun setAuthCode(authCode: String?) = if (authCode == null) null else stringToByteArray(authCode)

//    val requestKsnetSocketCommunicateDTO = RequestTmsDTO.KsnetSocketCommunicate(
//        telegramType = if (result == PaymentType.Approve.status) PaymentType.Approve.code.toByteArray() else PaymentType.Refund.code.toByteArray(),
//        telegramNo = generateString(12).toByteArray(),
//        payType = installment!!.toByteArray(),
//        totalAmount = stringAccountToByteArray(amount.toString()),
//        amount = stringAccountToByteArray((amount!! - (amount!! / 11)).toString()),
//        taxAmount = stringAccountToByteArray((amount!! / 11).toString()),
//        serviceAmount = stringAccountToByteArray("0"),
//        freeAmount = stringAccountToByteArray("0"),
//        rootAuthCode = if (authCd == null) null else authCd!!.toByteArray(),
//        rootRegDate = if (regDay == null) null else regDay!!.substring(2, 8).toByteArray(),
//        signTran = if (amount!! > 50000) "S".toByteArray() else "N".toByteArray()
//    )
}