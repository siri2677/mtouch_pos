package com.example.cleanarchitech_text_0506.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleanarchitech_text_0506.annotation.Bluetooth
import com.example.cleanarchitech_text_0506.annotation.Usb
import com.example.cleanarchitech_text_0506.deviceinterface.DeviceConnectService
import com.example.cleanarchitech_text_0506.deviceinterface.DeviceScan
import com.example.cleanarchitech_text_0506.deviceinterface.DeviceServiceController
import com.example.cleanarchitech_text_0506.enum.DeviceType
import com.example.cleanarchitech_text_0506.enum.PaymentType
import com.example.cleanarchitech_text_0506.enum.SerialCommunicationMessage
import com.example.cleanarchitech_text_0506.enum.TransactionType
import com.example.cleanarchitech_text_0506.sealed.Device
import com.example.cleanarchitech_text_0506.sealed.DeviceConnectSharedFlow
import com.example.cleanarchitech_text_0506.sealed.DeviceList
import com.example.cleanarchitech_text_0506.util.EncMSRManager
import com.example.cleanarchitech_text_0506.vo.CompletePaymentViewVO
import com.example.cleanarchitech_text_0506.vo.KsnetSocketCommunicationDTO
import com.example.domain.dto.request.tms.RequestCancelPaymentDTO
import com.example.domain.dto.request.tms.RequestInsertPaymentDataDTO
import com.example.domain.dto.request.tms.RequestPaymentDTO
import com.example.domain.repositoryInterface.DeviceSettingSharedPreference
import com.example.domain.repositoryInterface.OfflinePaymentRepository
import com.example.domain.sealed.ResponseTmsAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.Random
import javax.inject.Inject


@HiltViewModel
class DeviceCommunicationViewModel @Inject constructor(
    private val deviceSettingSharedPreference: DeviceSettingSharedPreference,
    @Bluetooth private val bluetoothServiceController: DeviceServiceController,
    @Usb private val usbServiceController: DeviceServiceController,
    @Bluetooth private val bluetoothDeviceScan: DeviceScan,
    @Usb private val usbDeviceScan: DeviceScan,
    private val offlinePaymentRepository: OfflinePaymentRepository
): ViewModel(), Serializable {

    private lateinit var deviceServiceController: DeviceServiceController
    private lateinit var deviceScan: DeviceScan
    private lateinit var serialCommunicationMessage: String
    lateinit var job: Job

    val deviceConnectSharedFlow = MutableSharedFlow<DeviceConnectSharedFlow>()
    var deviceConnectScanFlow = MutableSharedFlow<DeviceList>()
        get() = deviceScan.listUpdate
    val responseTmsAPI = MutableSharedFlow<ResponseTmsAPI>()

    fun setDeviceType(deviceType: String = getCurrentRegisteredDeviceType()): DeviceCommunicationViewModel? {
        return when(deviceType) {
            DeviceType.Bluetooth.name -> {
                deviceServiceController = bluetoothServiceController
                deviceScan = bluetoothDeviceScan
                this
            }
            DeviceType.Usb.name -> {
                deviceServiceController = usbServiceController
                deviceScan = usbDeviceScan
                this
            }
            else -> {
                null
            }
        }
    }

    fun connect(device: Device){
        deviceServiceController.bindingService(
            process = {
                it?.setDeviceConnectSharedFlow(deviceConnectSharedFlow)
                when (device) {
                    is Device.Bluetooth -> {
                        deviceSettingSharedPreference.setKeepBluetoothConnection(true)
                        deviceSettingSharedPreference.setCurrentRegisteredDeviceType(
                            DeviceType.Bluetooth.name,
                            device.device.address
                        )
                        usbServiceController.unBindingService()
                        usbServiceController.disConnect()
                        deviceServiceController.connect(device.device.address)
                    }

                    is Device.USB -> {
                        deviceSettingSharedPreference.setCurrentRegisteredDeviceType(
                            DeviceType.Usb.name,
                            device.device.toString()
                        )
                        bluetoothServiceController.unBindingService()
                        bluetoothServiceController.disConnect()
                        deviceServiceController.connect(device.device.toString())
                    }
                }
            }
        )
    }

    fun disConnect(){
        deviceServiceController.unBindingService()
        deviceServiceController.disConnect()
    }

    fun bluetoothDeviceResister(bluetoothDevice: BluetoothDevice) {
        deviceSettingSharedPreference.setKeepBluetoothConnection(false)
        deviceSettingSharedPreference.setCurrentRegisteredDeviceType(DeviceType.Bluetooth.name, bluetoothDevice.address)
    }
    fun bluetoothDeviceUnResister() { deviceSettingSharedPreference.clearCurrentRegisteredDeviceType() }

    fun scan() { deviceScan.scan() }
    fun scanCancel() { deviceScan.cancel() }

    fun serviceUnbind() { deviceServiceController.unBindingService() }
    fun requestOfflinePayment(
        requestInsertPaymentDataDTO: RequestInsertPaymentDataDTO,
        byteArray: ByteArray = EncMSRManager().makeDongleInfo()
    ) {
        offlinePaymentRepository.approve(
            onSuccess = {
                val totalAmt = requestInsertPaymentDataDTO.amount!!
                val vat = totalAmt / 11
                val supply = totalAmt - vat
                val ksnetSocketCommunicationDTO = KsnetSocketCommunicationDTO(
                    transType = "IC".toByteArray(),
                    telegramType = "0200".toByteArray(),
                    telegramNo = generateString(12).toByteArray(),
                    dptID = it.secondKey!!.toByteArray(),
                    payType = requestInsertPaymentDataDTO.installment!!.toByteArray(),
                    trackId = it.trackId!!.toByteArray(),
                    totalAmount = stringAccountToByteArray(totalAmt.toString()),
                    amount = stringAccountToByteArray(supply.toString()),
                    serviceAmount = stringAccountToByteArray("0"),
                    taxAmount = stringAccountToByteArray(vat.toString()),
                    freeAmount = stringAccountToByteArray("0"),
                    signTrans = if(totalAmt > 50000) "S".toByteArray() else "N".toByteArray(),
                )

                requestInsertPaymentDataDTO.van = it.van
                requestInsertPaymentDataDTO.trackId = it.trackId
                requestInsertPaymentDataDTO.vanTrxId = it.trackId
                requestInsertPaymentDataDTO.vanId = it.vanId

                deviceServiceController.bindingService(
                    process = { it ->
                        val deviceConnectService = it.setEssentialData(ksnetSocketCommunicationDTO, deviceConnectSharedFlow)
                        afterSerialCommunicationProcess(deviceConnectService, requestInsertPaymentDataDTO)
                        when (deviceSettingSharedPreference.isKeepBluetoothConnection()) {
                            true -> {
                                deviceConnectService.sendData(byteArray)
                            }
                            false -> {
                                deviceConnectService.connectDevice(
                                    deviceSettingSharedPreference.getCurrentRegisteredDeviceInformation(),
                                    byteArray
                                )
                            }
                        }
                    }
                )
            },
            onError = { viewModelScope.launch { responseTmsAPI.emit(ResponseTmsAPI.ErrorMessage(it)) }},
            body = RequestPaymentDTO(
                amount = requestInsertPaymentDataDTO.amount.toString(),
                installment = requestInsertPaymentDataDTO.installment,
                token = requestInsertPaymentDataDTO.token,
            )
        )
    }

    fun requestOfflinePaymentCancel(
        requestInsertPaymentDataDTO: RequestInsertPaymentDataDTO,
        byteArray: ByteArray = EncMSRManager().makeDongleInfo()
    ) {
        offlinePaymentRepository.refund(
            onSuccess = {
                val totalAmt = requestInsertPaymentDataDTO.amount!!
                val vat = totalAmt / 11
                val supply = totalAmt - vat
                val ksnetSocketCommunicationDTO = KsnetSocketCommunicationDTO(
                    transType = "IC".toByteArray(),
                    telegramType = "0420".toByteArray(),
                    telegramNo = generateString(12).toByteArray(),
                    dptID = it.secondKey!!.toByteArray(),
                    payType = requestInsertPaymentDataDTO.installment!!.toByteArray(),
                    trackId = it.trackId!!.toByteArray(),
                    totalAmount = stringAccountToByteArray(totalAmt.toString()),
                    amount = stringAccountToByteArray(supply.toString()),
                    serviceAmount = stringAccountToByteArray("0"),
                    taxAmount = stringAccountToByteArray(vat.toString()),
                    freeAmount = stringAccountToByteArray("0"),
                    authNum = requestInsertPaymentDataDTO.authCd!!.toByteArray(),
                    authDate = requestInsertPaymentDataDTO.regDate!!.toByteArray(),
                    signTrans = if(totalAmt > 50000) "S".toByteArray() else "N".toByteArray(),
                )

                requestInsertPaymentDataDTO.van = it.van
                requestInsertPaymentDataDTO.trackId = it.trackId
                requestInsertPaymentDataDTO.vanTrxId = it.trackId
                requestInsertPaymentDataDTO.vanId = it.vanId

                deviceServiceController.bindingService(
                    process = { it ->
                        val deviceConnectService = it.setEssentialData(ksnetSocketCommunicationDTO, deviceConnectSharedFlow)
                        afterSerialCommunicationProcess(deviceConnectService, requestInsertPaymentDataDTO)
                        when (deviceSettingSharedPreference.isKeepBluetoothConnection()) {
                            true -> {
                                deviceConnectService.sendData(byteArray)
                            }
                            false -> {
                                deviceConnectService.connectDevice(
                                    deviceSettingSharedPreference.getCurrentRegisteredDeviceInformation(),
                                    byteArray
                                )
                            }
                        }
                    }
                )
            },
            onError = { viewModelScope.launch { responseTmsAPI.emit(ResponseTmsAPI.ErrorMessage(it)) }},
            body = RequestCancelPaymentDTO(
                amount = requestInsertPaymentDataDTO.amount.toString(),
                installment = requestInsertPaymentDataDTO.installment,
                trxId = requestInsertPaymentDataDTO.trxId,
                token = requestInsertPaymentDataDTO.token
            )
        )
    }

    private fun afterSerialCommunicationProcess(
        deviceConnectService: DeviceConnectService,
        requestInsertPaymentDataDTO: RequestInsertPaymentDataDTO
    ) {
        if(::job.isInitialized){
            if(!job.isActive) job.start()
        } else {
            job = CoroutineScope(Dispatchers.IO).launch(start = CoroutineStart.LAZY) {
                deviceConnectSharedFlow.collect{
                    when(it) {
                        is DeviceConnectSharedFlow.SerialCommunicationMessageFlow -> {
                            when (it.message) {
                                SerialCommunicationMessage.IcCardInsertRequest.message -> {
                                    deviceConnectService.sendData(
                                        EncMSRManager().makeCardNumSendReq(
                                            String.format("%09d", requestInsertPaymentDataDTO.amount!!).toByteArray(),
                                            "10".toByteArray()
                                        )
                                    )
                                }
                                SerialCommunicationMessage.FallBackMessage.message -> {
                                    deviceConnectService.sendData(
                                        EncMSRManager().makeFallBackCardReq(it.getData(), "99")
                                    )
                                }
                            }
                        }
                        is DeviceConnectSharedFlow.RequestSocketCommunication -> {
                            requestInsertPaymentDataDTO.number = it.ksnetSocketCommunicationDTO.cardBin!!
                            offlinePaymentRepository.push(
                                onSuccess = {
                                    viewModelScope.launch{
                                        deviceConnectSharedFlow.emit(DeviceConnectSharedFlow.SerialCommunicationMessageFlow(SerialCommunicationMessage.CompletePayment.message))
                                        delay(300)
                                        deviceConnectSharedFlow.emit(
                                            DeviceConnectSharedFlow.PaymentCompleteFlow(
                                                CompletePaymentViewVO(
                                                    transactionType = TransactionType.Offline,
                                                    paymentType =
                                                        if(requestInsertPaymentDataDTO.type == PaymentType.Refund.value) {
                                                            PaymentType.Refund
                                                        } else {
                                                            PaymentType.Approve
                                                        },
                                                    installment = requestInsertPaymentDataDTO.installment,
                                                    trackId = requestInsertPaymentDataDTO.trackId,
                                                    cardNumber = requestInsertPaymentDataDTO.number,
                                                    amount = requestInsertPaymentDataDTO.amount.toString(),
                                                    regDay = it.regDay!!,
                                                    authCode = it.authCode!!,
                                                    trxId = it.trxId,
                                                )
                                            )
                                        )
                                    }
                                },
                                onError = {
                                    viewModelScope.launch {
                                        responseTmsAPI.emit(ResponseTmsAPI.ErrorMessage(it))
                                    }
                                },
                                body = requestInsertPaymentDataDTO,
                                requestTelegram = EncMSRManager().makeRequestTelegram(it.ksnetSocketCommunicationDTO)
                            )
                        }
                        else -> {}
                    }
                }
            }
            job.start()
        }
    }

    fun getCurrentRegisteredDeviceType() = deviceSettingSharedPreference.getCurrentRegisteredDeviceType()
    fun init() { deviceServiceController.bindingService(process = { it?.init() }) }
    private fun stringAccountToByteArray(account: String) = String.format("%012d", account.toLong()).toByteArray()
    private fun generateString(length: Int): String {
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


}