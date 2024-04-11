package com.example.mtouchpos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.request.tms.RequestTmsModel
import com.example.domain.model.response.ResponseModel
import com.example.domain.model.response.tms.ResponseTmsModel
import com.example.domain.repositoryInterface.DeviceSettingSharedPreference
import com.example.domain.repositoryInterface.RequestRemoteRepository
import com.example.domain.repositoryInterface.UserInformationSharedPreference
import com.example.mtouchpos.FlowManager.Companion.deviceConnectSharedFlow
import com.example.mtouchpos.FlowManager.Companion.deviceSerialCommunicate
import com.example.mtouchpos.device.DeviceConnectManagerFactory
import com.example.mtouchpos.device.ResponseSerialCommunicationFormat
import com.example.mtouchpos.device.deviceinterface.DeviceCommunicateManager
import com.example.mtouchpos.dto.AmountInfo
import com.example.mtouchpos.dto.ResponseFlowData
import com.example.mtouchpos.dto.RootPaymentInfo
import com.example.mtouchpos.hilt.TmsRepository
import com.example.mtouchpos.mapper.RequestDataMapper
import com.example.mtouchpos.vo.DeviceConnectSharedFlow
import com.example.mtouchpos.vo.DeviceSerialCommunicate
import com.example.mtouchpos.vo.PaymentType
import com.example.mtouchpos.vo.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.mapstruct.factory.Mappers
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random
import javax.inject.Inject
import kotlin.experimental.and

@HiltViewModel
class DeviceCommunicationViewModel @Inject constructor(
    val deviceSettingSharedPreference: DeviceSettingSharedPreference,
    val userInformationSharedPreference: UserInformationSharedPreference,
    private val deviceCommunicateManager: DeviceCommunicateManager,
    private val deviceConnectManagerFactory: DeviceConnectManagerFactory,
    @TmsRepository private val RequestRemoteTmsRepositoryImpl: RequestRemoteRepository
) : ViewModel(), Serializable {
    init {
        deviceSerialCommunicate.value = DeviceSerialCommunicate.Init
        deviceConnectSharedFlow.value = DeviceConnectSharedFlow.Init
    }

    private val responseSerialCommunicationFormat = ResponseSerialCommunicationFormat()
    private val responseModel = MutableStateFlow<ResponseModel>(ResponseTmsModel.Init)
    val responseFlowData = MutableStateFlow<ResponseFlowData>(ResponseFlowData.Init)

    fun requestOfflinePayment(amountInfo: AmountInfo) {
        deviceSettingSharedPreference.getDeviceConnectSetting()!!.run {
            val requestPayment = {
                RequestRemoteTmsRepositoryImpl(
                    requestModel = Mappers.getMapper(RequestDataMapper::class.java)
                        .toRequestPaymentModel(amountInfo),
                    responseModel = responseModel
                )
            }
            if (isKeepConnect) {
                requestPayment()
            } else {
                deviceConnectManagerFactory.getInstance(deviceType).connect(deviceInformation)
                handleDeviceConnect { requestPayment() }
            }
            handleResponseApprove(amountInfo, PaymentType.Approve)
        }
    }

    fun requestOfflineCancelPayment(
        amountInfo: AmountInfo,
        rootPaymentInfo: RootPaymentInfo
    ) {
        deviceSettingSharedPreference.getDeviceConnectSetting()!!.run {
            val requestPayment = {
                RequestRemoteTmsRepositoryImpl(
                    requestModel = Mappers.getMapper(RequestDataMapper::class.java)
                        .toRequestCancelPaymentModel(
                            amountInfo = amountInfo,
                            rootPaymentInfo = rootPaymentInfo
                        ),
                    responseModel = responseModel
                )
            }
            if (isKeepConnect) {
                requestPayment()
            } else {
                deviceConnectManagerFactory.getInstance(deviceType).connect(deviceInformation)
                handleDeviceConnect { requestPayment() }
            }
            handleResponseApprove(amountInfo, PaymentType.Refund)
        }
    }

    private fun handleResponseApprove(
        amountInfo: AmountInfo,
        paymentType: PaymentType
    ) {
        if (responseModel.value == ResponseTmsModel.Init) {
            viewModelScope.launch {
                responseModel.collect {
                    if (it is ResponseTmsModel.Payment) {
                        deviceCommunicateManager.sendData(EncMSRManager().makeDongleInfo())
                        handleResponsePayment(amountInfo, it)
                    }
                    if (it is ResponseTmsModel.KsnetSocketCommunicate) {
                        if (it.result == "정상등록") {
                            responseFlowData.emit(
                                Mappers.getMapper(RequestDataMapper::class.java)
                                    .toCompleteCreditPayment(
                                        transactionType = TransactionType.Offline,
                                        paymentType = paymentType,
                                        ksnetSocketCommunicate = it
                                    )
                            )
                        } else {
                            responseFlowData.emit(ResponseFlowData.Error(it.resultMsg!!))
                        }
                    }
                    if (it is ResponseTmsModel.Error) {
                        responseFlowData.emit(ResponseFlowData.Error(it.message))
                    }
                }
            }
        }
    }

    private fun handleDeviceConnect(requestPayment: () -> Unit) {
        viewModelScope.launch {
            deviceConnectSharedFlow.collect {
                if (it is DeviceConnectSharedFlow.ConnectCompleteFlow) {
                    requestPayment()
                    this.cancel()
                }
            }
        }
    }

    private fun handleResponsePayment(
        amountInfo: AmountInfo,
        responseTmsModel: ResponseTmsModel.Payment
    ) {
        if (deviceSerialCommunicate.value == DeviceSerialCommunicate.Init) {
            fun generateString(length: Int): String {
                val rnd = Random()
                val buf = StringBuffer()
                for (i in 0 until length) {
                    if (rnd.nextBoolean()) {
                        buf.append((rnd.nextInt(26) as Int + 97).toChar())
                    } else {
                        buf.append(rnd.nextInt(10))
                    }
                }
                return buf.toString()
            }

            viewModelScope.launch {
                deviceSerialCommunicate.collect {
                    when (it) {
                        is DeviceSerialCommunicate.SerialCommunicationMessage.IcCardInsertRequest -> {
                            deviceCommunicateManager.sendData(
                                EncMSRManager().makeCardNumSendReq(
                                    String.format("%09d", amountInfo.amount).toByteArray(),
                                    "99".toByteArray()
                                )
                            )
                        }

                        is DeviceSerialCommunicate.SerialCommunicationMessage.FallBackMessage -> {
                            deviceCommunicateManager.sendData(
                                EncMSRManager().makeFallBackCardReq(it.getData(), "99")
                            )
                        }

                        is DeviceSerialCommunicate.ResultSerial -> responseSerialCommunicationFormat.receiveData(
                            it.data
                        )

                        is DeviceSerialCommunicate.RequestSocketCommunication -> {
                            Log.w("test", responseTmsModel.secondKey)
                            RequestRemoteTmsRepositoryImpl(
                                responseModel = responseModel,
                                requestModel = RequestTmsModel.KsnetSocketCommunicate(
                                    RequestTmsModel.KsnetSocketCommunicateTms(
                                        Mappers.getMapper(RequestDataMapper::class.java)
                                            .toRequestKsnetSocketCommunicateDataModel(
                                                responseTmsModel = responseTmsModel,
                                                cardNumber = it.serialInfo.cardNumber
                                            ),
                                        Mappers.getMapper(RequestDataMapper::class.java)
                                            .toRequestKsnetSocketCommunicateSocketModel(
                                                telegramType = generateString(12),
                                                serviceAmount = 0,
                                                freeAmount = 0,
                                                responseTmsModel = responseTmsModel,
                                                serialInfo = it.serialInfo
                                            )
                                    )
                                )
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun serviceUnbind() {
        deviceCommunicateManager.unBindingService()
    }

    fun getCurrentRegisteredDeviceType() =
        deviceSettingSharedPreference.getDeviceConnectSetting()!!.deviceType

    class EncMSRManager {
        private val yymmddhhmmss = getTime()!!.substring(0, 12)
        private val stx: Byte = 0x02
        private val etx: Byte = 0x03.toByte()
        private val ksnet_dongle_info_req = 0xC0.toByte()
        private val KSNET_READER_SET_REQ = 0xC1.toByte()
        private val KSNET_CARDNO_REQ = 0xC2.toByte()
        private val KSNET_IC_2ND_REQ = 0xC3.toByte()
        private val KSNET_INTEGRITY_REQ = 0xC4.toByte()
        private val KSNET_FALLBACK_REQ = 0xC5.toByte()
        private val KSNET_IC_STATE_REQ = 0xC6.toByte()
        private val KSNET_KEY_SHARED_REQ = 0xC7.toByte()
        private val KSNET_Device_INFO_REQ = 0xCF.toByte()

        private var packet = ByteArray(1024)
        private var year: String? = getTime()?.substring(0, 2)

        private fun getTime(): String? {
            val time = System.currentTimeMillis()
            val dayTime = SimpleDateFormat("yyMMddhhmmss")
            return dayTime.format(Date(time))
        }

        private fun lrc(bytes: ByteArray, length: Int): Int {
            var checksum = 0
            for (i in 1 until length) {
                checksum = checksum xor ((bytes[i] and 0xFF.toByte()).toInt())
            }
            return checksum
        }

        fun makeDongleInfo(): ByteArray {
            var packet = ByteArray(1024)
            var idx = 0
            packet[idx++] = stx //STX
            packet[idx++] = 0x00 //Length 2바이트
            packet[idx++] = 0x05
            packet[idx++] = ksnet_dongle_info_req // 'C0' Command
            packet[idx++] = year!![0].toByte()
            packet[idx++] = year!![1].toByte()
            packet[idx++] = '1'.toByte() //카드데이터형식 1:카드번호 마스킹 2: 논마스킹 3: 16자리 암호화 + 마스킹
            packet[idx++] = etx
            packet[idx++] = lrc(packet, idx).toByte()
            val txPacket = ByteArray(idx)
            System.arraycopy(packet, 0, txPacket, 0, idx)
            packet = ByteArray(1024) //패킷 초기화

            return txPacket
        }

        fun makeCardNumSendReq(totalAmount: ByteArray, resTime: ByteArray): ByteArray {
            var idx = 0
            val YYMMDDhhmmss = getTime()!!.substring(0, 12)

            packet[idx++] = stx //STX
            packet[idx++] = 0x00 //Length 2바이트
            packet[idx++] = 0x19
            packet[idx++] = KSNET_CARDNO_REQ // 'C2' Command


            System.arraycopy(YYMMDDhhmmss.toByteArray(), 0, packet, idx, 12)
            idx += 12
            System.arraycopy(totalAmount, 0, packet, idx, 9)
            idx += 9
            System.arraycopy(resTime, 0, packet, idx, 2)
            idx += 2

            packet[idx++] = etx
            val bLRC = lrc(packet, idx).toByte()
            packet[idx++] = bLRC //LRC


            val txPacket = ByteArray(idx)
            System.arraycopy(packet, 0, txPacket, 0, idx)

            packet = ByteArray(1024) //패킷 초기화

            return txPacket
        }

        fun makeFallBackCardReq(
            FallBcack_ErrCode: String,
            timeOut: String,
        ): ByteArray {
            var idx = 0
            packet[idx++] = stx //STX
            System.arraycopy(make2ByteLengh(25), 0, packet, idx, 2)
            val idx2 = idx + 2
            val idx3 = idx2 + 1
            packet[idx2] = KSNET_FALLBACK_REQ
            System.arraycopy(yymmddhhmmss.toByteArray(), 0, packet, idx3, 12)
            val idx4 = idx3 + 12
            System.arraycopy(
                String.format("%09d", *arrayOf<Any>(Integer.valueOf(FallBcack_ErrCode.toInt())))
                    .toByteArray(), 0,
                packet, idx4, 9
            )
            val idx5 = idx4 + 9
            System.arraycopy(timeOut.toByteArray(), 0, packet, idx5, 2)
            val idx6 = idx5 + 2
            val idx7 = idx6 + 1
            packet[idx6] = 3
            val idx8 = idx7 + 1
            packet[idx7] = lrc(packet, idx7).toByte()
            val txPacket = ByteArray(idx8)
            System.arraycopy(packet, 0, txPacket, 0, idx8)
            packet = ByteArray(1024)
            return txPacket
        }

        private fun make2ByteLengh(i: Int): ByteArray? {
            val bArr = ByteArray(2)
            bArr[1] = i.toByte()
            bArr[0] = (i ushr 8).toByte()
            return bArr
        }
    }
}