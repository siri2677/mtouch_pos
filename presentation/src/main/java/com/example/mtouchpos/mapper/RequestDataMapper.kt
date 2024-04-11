package com.example.mtouchpos.mapper


import com.example.domain.model.request.pay.RequestPayModel
import com.example.domain.model.request.tms.RequestTmsModel
import com.example.domain.model.response.pay.ResponsePayModel
import com.example.domain.model.response.tms.ResponseTmsModel
import com.example.mtouchpos.dto.AmountInfo
import com.example.mtouchpos.dto.DirectPaymentInfo
import com.example.mtouchpos.dto.LoginInfo
import com.example.mtouchpos.dto.PaymentPeriod
import com.example.mtouchpos.dto.ResponseFlowData
import com.example.mtouchpos.dto.RootPaymentInfo
import com.example.mtouchpos.dto.SerialInfo
import com.example.mtouchpos.vo.PaymentType
import com.example.mtouchpos.vo.TransactionType
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import java.util.Date


@Mapper(uses = [MapperConversion::class])
interface RequestDataMapper {
    fun toRequestGetUserInformationModel(
        loginInfo: LoginInfo
    ): RequestTmsModel.GetUserInformation

    fun toLoginInfo(
        loginInformation: ResponseTmsModel.GetUserInformation
    ): LoginInfo

    fun toLoginInfo(
        loginInformation: RequestTmsModel.GetUserInformation
    ): LoginInfo

    fun toRequestPaymentModel(
        amountInfo: AmountInfo
    ): RequestTmsModel.Payment

    @Mapping(target = "trxId", source = "rootPaymentInfo.rootTrxId")
    fun toRequestCancelPaymentModel(
        amountInfo: AmountInfo,
        rootPaymentInfo: RootPaymentInfo
    ): RequestTmsModel.CancelPayment
    
    fun toRequestKsnetSocketCommunicateDataModel(
        responseTmsModel: ResponseTmsModel.Payment,
        cardNumber: String,
        walletSettle: String = "N",
        vanPayment: String = "false"
    ): RequestTmsModel.KsnetSocketCommunicateData

    @Mapping(target = "dptId", source = "responseTmsModel.secondKey", qualifiedByName = ["stringToByteArray"])
    @Mapping(target = "totalAmount", source = "responseTmsModel.amount", qualifiedByName = ["intToByteArray"])
    @Mapping(target = "amount", source = "responseTmsModel.amount", qualifiedByName = ["setSupplyAmount"])
    @Mapping(target = "taxAmount", source = "responseTmsModel.amount", qualifiedByName = ["setTaxAmount"])
    @Mapping(target = "serviceAmount", source = "serviceAmount", qualifiedByName = ["intToByteArray"])
    @Mapping(target = "freeAmount", source = "freeAmount", qualifiedByName = ["intToByteArray"])
    @Mapping(target = "signTran", source = "responseTmsModel.amount", qualifiedByName = ["setSignTran"])
    @Mapping(target = "telegramType", source = "responseTmsModel.result", qualifiedByName = ["setTelegramType"])
    @Mapping(target = "payType", source = "responseTmsModel.installment", qualifiedByName = ["stringToByteArray"])
    @Mapping(target = "rootAuthCode", source = "responseTmsModel.authCd", qualifiedByName = ["setAuthCode"])
    @Mapping(target = "rootRegDate", source = "responseTmsModel.regDay", qualifiedByName = ["setRegDay"])
    fun toRequestKsnetSocketCommunicateSocketModel(
        telegramType: String,
        serviceAmount: Int,
        freeAmount: Int,
        responseTmsModel: ResponseTmsModel.Payment,
        serialInfo: SerialInfo,
        transType: ByteArray = "IC".toByteArray(),
        swModelNumber: ByteArray = "######MTOUCH1101".toByteArray(),
        receiptNo: ByteArray = "".toByteArray(),
        workType: ByteArray = "01".toByteArray(),
        posEntry: ByteArray = "S".toByteArray(),
        filler: ByteArray = "".toByteArray(),
        signData: ByteArray = "".toByteArray()
    ): RequestTmsModel.KsnetSocketCommunicateSocket

    fun toRequestDirectPaymentPay(
        amountInfo: AmountInfo,
        directPaymentInfo: DirectPaymentInfo,
        card: RequestPayModel.DirectPaymentPayCard = toRequestDirectPaymentCard(amountInfo, directPaymentInfo),
        product: RequestPayModel.DirectPaymentPayProduct = toRequestDirectPaymentProduct(directPaymentInfo),
        metadata: RequestPayModel.DirectPaymentPayMetadata = toRequestDirectPaymentMetadata(directPaymentInfo),
        trxType: String = "ONTR",
        trackId: String = "AXD_" + Date().time
    ): RequestPayModel.DirectPaymentPay

    @Mapping(target = "number", source = "directPaymentInfo.cardNumber")
    fun toRequestDirectPaymentCard(
        amountInfo: AmountInfo,
        directPaymentInfo: DirectPaymentInfo,
    ): RequestPayModel.DirectPaymentPayCard

    @Mapping(target = "name", source = "directPaymentInfo.productName")
    fun toRequestDirectPaymentProduct(
        directPaymentInfo: DirectPaymentInfo,
    ): RequestPayModel.DirectPaymentPayProduct

    fun toRequestDirectPaymentMetadata(
        directPaymentInfo: DirectPaymentInfo,
    ): RequestPayModel.DirectPaymentPayMetadata

    fun toRequestDirectCancelPaymentRefund(
        amountInfo: AmountInfo,
        rootPaymentInfo: RootPaymentInfo,
        trxType: String = "ONTR",
        trackId: String = "AXD_" + Date().time
    ): RequestPayModel.DirectCancelPaymentRefund

    fun toRequestGetPaymentListEntity(
        paymentPeriod: PaymentPeriod
    ): RequestTmsModel.GetPaymentList

//    fun toRequestGetMerchantNameEntity(requestMerchantNameDto: RequestTmsDTO.GetMerchantName): RequestTmsModel.GetMerchantName
//    fun toRequestGetPaymentStatisticsEntity(requestGetPaymentStatisticsDto: RequestTmsDTO.GetPaymentStatistics): RequestTmsModel.GetPaymentStatistics
//    fun toRequestDirectPaymentCheckEntity(requestDirectPaymentCheckDto: RequestTmsDTO.DirectPaymentCheck): RequestTmsModel.DirectPaymentCheck

    @Mapping(target = "cardNumber", source = "ksnetSocketCommunicateResultData.cardNum")
    @Mapping(target = "amount", source = "ksnetSocketCommunicateResultData.totalAmount")
    @Mapping(target = "authCode", source = "ksnetSocketCommunicateResultData.authNum")
    @Mapping(target = "paymentType", source = "paymentType")
    fun toCompleteCreditPayment(
        transactionType: TransactionType,
        paymentType: PaymentType,
        ksnetSocketCommunicate: ResponseTmsModel.KsnetSocketCommunicate,
        ksnetSocketCommunicateResultData: ResponseTmsModel.KsnetSocketCommunicateResultData = ksnetSocketCommunicate.resultData!!
    ): ResponseFlowData.CompletePayment

    @Mapping(target = "authDate", source = "directPaymentResult.create")
    @Mapping(target = "authCode", source = "directPaymentPay.authCd")
    @Mapping(target = "installment", source = "directPaymentCard.installment")
    fun toCompleteDirectPayment(
        transactionType: TransactionType,
        paymentType: PaymentType,
        directPayment: ResponsePayModel.DirectPayment,
        cardNumber: String = "${directPayment.pay!!.card.bin}${"**********"}${directPayment.pay!!.card.last4}",
        directPaymentResult: ResponsePayModel.DirectPaymentResult = directPayment.result,
        directPaymentPay: ResponsePayModel.DirectPaymentPay = directPayment.pay!!,
        directPaymentCard: ResponsePayModel.DirectPaymentCard = directPaymentPay.card
    ): ResponseFlowData.CompletePayment

    @Mapping(target = "authDate", source = "directPaymentResult.create")
    @Mapping(target = "authCode", source = "directCancelPaymentRefund.authCd")
    fun toCompleteDirectCancelPayment(
        transactionType: TransactionType,
        paymentType: PaymentType,
        cardNumber: String,
        installment: String,
        directCancelPayment: ResponsePayModel.DirectCancelPayment,
        directPaymentResult: ResponsePayModel.DirectPaymentResult = directCancelPayment.result,
        directCancelPaymentRefund: ResponsePayModel.DirectCancelPaymentRefund = directCancelPayment.refund!!,
    ): ResponseFlowData.CompletePayment

    fun toCreditPayment(
        paymentContents: ResponseTmsModel.PaymentContents
    ): ResponseFlowData.CreditPayment

    fun toFailCompleteCreditPayment(
        transactionType: TransactionType,
        paymentType: PaymentType,
        ksnetSocketCommunicate: ResponseTmsModel.KsnetSocketCommunicate
    ): ResponseFlowData.CompletePayment


}