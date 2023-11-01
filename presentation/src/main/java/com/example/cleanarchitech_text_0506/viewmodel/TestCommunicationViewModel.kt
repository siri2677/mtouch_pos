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
import com.example.cleanarchitech_text_0506.enum.SerialCommunicationMessage
import com.example.cleanarchitech_text_0506.sealed.Device
import com.example.cleanarchitech_text_0506.sealed.DeviceConnectSharedFlow
import com.example.cleanarchitech_text_0506.sealed.DeviceList
import com.example.cleanarchitech_text_0506.util.EncMSRManager
import com.example.cleanarchitech_text_0506.vo.KsnetSocketCommunicationDTO
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject


@HiltViewModel
class TestCommunicationViewModel @Inject constructor(
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

    var deviceConnectSharedFlow = MutableSharedFlow<DeviceConnectSharedFlow>()
//        get() = deviceServiceController.deviceConnectSharedFlow
    var deviceConnectScanFlow = MutableSharedFlow<DeviceList>()
        get() = deviceScan.listUpdate
    var responseTmsAPI = MutableSharedFlow<ResponseTmsAPI>()


    fun getSerialCommunicationMessage() = serialCommunicationMessage
    fun setSerialCommunicationMessage(message: String) {this.serialCommunicationMessage = message}
    fun setDeviceType(deviceType: String = getCurrentRegisteredDeviceType()): TestCommunicationViewModel? {
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
    fun sendData(
        requestPaymentDTO: RequestPaymentDTO,
        byteArray: ByteArray
    ) {
        offlinePaymentRepository.approve(
            onSuccess = {
                val totalAmt = requestPaymentDTO.amount!!
                val vat = totalAmt.toInt() / 11
                val supply = totalAmt.toInt() - vat

                val requestInsertPaymentDataDTO = RequestInsertPaymentDataDTO(
                    amount = requestPaymentDTO.amount!!.toInt(),
                    van = it.van!!,
                    vanTrxId = it.trackId!!,
                    trackId = it.trackId!!,
                    type = "승인",
                    vanId = it.vanId!!,
                    installment = requestPaymentDTO.installment!!,
                    token = requestPaymentDTO.token,
                    prodQty = requestPaymentDTO.prodQty,
                    prodName = requestPaymentDTO.prodName,
                    prodPrice = requestPaymentDTO.prodPrice,
                    payerTel = requestPaymentDTO.payerTel,
                    payerName = requestPaymentDTO.payerName,
                    payerEmail = requestPaymentDTO.payerEmail
                )

                val ksnetSocketCommunicationDTO = KsnetSocketCommunicationDTO(
                    transType = "IC".toByteArray(),
                    telegramType = "0200".toByteArray(),
                    workType = "01".toByteArray(),
                    posEntry = "S".toByteArray(),
                    dptID = it.secondKey!!.toByteArray(),
                    payType = "00".toByteArray(),
                    totalAmount = getStrMoneytoTgAmount(totalAmt),
                    amount = getStrMoneytoTgAmount(supply.toString()),
                    serviceAmount = getStrMoneytoTgAmount("0"),
                    taxAmount = getStrMoneytoTgAmount(vat.toString()),
                    freeAmount = getStrMoneytoTgAmount("0"),
                    signTrans = if(totalAmt.toInt() > 50000) "S".toByteArray() else "N".toByteArray(),
                    installment = requestPaymentDTO.installment!!
                )

                deviceServiceController.bindingService(
                    process = { it ->
                        afterSerialCommunicationProcess(it, ksnetSocketCommunicationDTO, requestInsertPaymentDataDTO)
                        when (deviceSettingSharedPreference.isKeepBluetoothConnection()) {
                            true -> {
                                it?.setEssentialData(ksnetSocketCommunicationDTO, deviceConnectSharedFlow)?.sendData(byteArray)
                            }
                            false -> {
                                it?.setEssentialData(ksnetSocketCommunicationDTO, deviceConnectSharedFlow)?.connectDevice(
                                    deviceSettingSharedPreference.getCurrentRegisteredDeviceInformation(),
                                    byteArray
                                )
                            }
                        }
                    }
                )

            },
            onError = { viewModelScope.launch { responseTmsAPI.emit(ResponseTmsAPI.ErrorMessage(it)) }},
            body = requestPaymentDTO
        )
    }

    fun afterSerialCommunicationProcess(
        deviceConnectService: DeviceConnectService,
        ksnetSocketCommunicationDTO: KsnetSocketCommunicationDTO,
        requestInsertPaymentDataDTO: RequestInsertPaymentDataDTO
    ) {
        try{
            if(!job.isActive) job.start()
        } catch (e: Exception) {
            job = CoroutineScope(Dispatchers.IO).launch(start = CoroutineStart.LAZY) {
                deviceConnectSharedFlow.collect{
                    when(it) {
                        is DeviceConnectSharedFlow.SerialCommunicationMessageFlow -> {
                            when (it.flow) {
                                SerialCommunicationMessage.IcCardInsertRequest.message -> {
                                    deviceConnectService.setEssentialData(
                                        ksnetSocketCommunicationDTO, deviceConnectSharedFlow
                                    )?.sendData(
                                        EncMSRManager().makeCardNumSendReq(
                                            getStrMoneytoTgAmount(String(ksnetSocketCommunicationDTO.totalAmount!!, Charsets.UTF_8)),
                                            "10".toByteArray()
                                        )
                                    )
                                }
                                SerialCommunicationMessage.FallBackMessage.message -> {
                                    deviceConnectService.setEssentialData(
                                        ksnetSocketCommunicationDTO, deviceConnectSharedFlow
                                    )?.sendData(
                                        EncMSRManager().makeFallBackCardReq(it.getData(), "99")
                                    )
                                }
                            }
                        }
                        is DeviceConnectSharedFlow.RequestSocketCommunication -> {
                            offlinePaymentRepository.push(
                                onSuccess = { viewModelScope.launch{ responseTmsAPI.emit(ResponseTmsAPI.InsertPaymentData(it)) }},
                                onError = { viewModelScope.launch { responseTmsAPI.emit(ResponseTmsAPI.ErrorMessage(it)) }},
                                body = requestInsertPaymentDataDTO,
                                requestTelegram = it.byteArray
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

    fun getStrMoneytoTgAmount(account: String): ByteArray {
        return String.format("%012d", account.toLong()).toByteArray()
    }


}