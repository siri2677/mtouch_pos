package com.example.cleanarchitech_text_0506.vo

data class KsnetSocketCommunicationDTO(
    var transType: ByteArray,
    var telegramType: ByteArray,
    var workType: ByteArray,
    var telegramNo: ByteArray = "".toByteArray(),
    var posEntry: ByteArray,
    var authNum: ByteArray = "".toByteArray(),
    var authDate: ByteArray= "".toByteArray(),
    var trackId: ByteArray = "".toByteArray(),
    var dptID: ByteArray,
    var swModelNum: ByteArray = "######MTOUCH1101".toByteArray(),
    var readerModelNum: ByteArray = "".toByteArray(),
    var payType: ByteArray,
    var totalAmount: ByteArray,
    var amount: ByteArray,
    var installment: String,
    var serviceAmount: ByteArray,
    var taxAmount: ByteArray,
    var freeAmount: ByteArray,
    var filler: ByteArray = "".toByteArray(),
    var signTrans: ByteArray,
    var signData: ByteArray? = null,
    var encryptInfo: ByteArray = "".toByteArray(),
    var emvData: ByteArray = "".toByteArray(),
    var trackII: ByteArray = " ".toByteArray()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KsnetSocketCommunicationDTO

        if (!transType.contentEquals(other.transType)) return false
        if (!telegramType.contentEquals(other.telegramType)) return false
        if (!workType.contentEquals(other.workType)) return false
        if (!telegramNo.contentEquals(other.telegramNo)) return false
        if (!posEntry.contentEquals(other.posEntry)) return false
        if (!authNum.contentEquals(other.authNum)) return false
        if (!authDate.contentEquals(other.authDate)) return false
        if (!trackId.contentEquals(other.trackId)) return false
        if (!dptID.contentEquals(other.dptID)) return false
        if (!swModelNum.contentEquals(other.swModelNum)) return false
        if (!readerModelNum.contentEquals(other.readerModelNum)) return false
        if (!payType.contentEquals(other.payType)) return false
        if (!totalAmount.contentEquals(other.totalAmount)) return false
        if (!amount.contentEquals(other.amount)) return false
        if (!serviceAmount.contentEquals(other.serviceAmount)) return false
        if (!taxAmount.contentEquals(other.taxAmount)) return false
        if (!freeAmount.contentEquals(other.freeAmount)) return false
        if (!filler.contentEquals(other.filler)) return false
        if (!signTrans.contentEquals(other.signTrans)) return false
        if (!signData.contentEquals(other.signData)) return false
        if (!encryptInfo.contentEquals(other.encryptInfo)) return false
        if (!emvData.contentEquals(other.emvData)) return false
        if (!trackII.contentEquals(other.trackII)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = transType.contentHashCode()
        result = 31 * result + telegramType.contentHashCode()
        result = 31 * result + workType.contentHashCode()
        result = 31 * result + telegramNo.contentHashCode()
        result = 31 * result + posEntry.contentHashCode()
        result = 31 * result + authNum.contentHashCode()
        result = 31 * result + authDate.contentHashCode()
        result = 31 * result + trackId.contentHashCode()
        result = 31 * result + dptID.contentHashCode()
        result = 31 * result + swModelNum.contentHashCode()
        result = 31 * result + readerModelNum.contentHashCode()
        result = 31 * result + payType.contentHashCode()
        result = 31 * result + totalAmount.contentHashCode()
        result = 31 * result + amount.contentHashCode()
        result = 31 * result + serviceAmount.contentHashCode()
        result = 31 * result + taxAmount.contentHashCode()
        result = 31 * result + freeAmount.contentHashCode()
        result = 31 * result + filler.contentHashCode()
        result = 31 * result + signTrans.contentHashCode()
        result = 31 * result + signData.contentHashCode()
        result = 31 * result + encryptInfo.contentHashCode()
        result = 31 * result + emvData.contentHashCode()
        result = 31 * result + trackII.contentHashCode()
        return result
    }
}