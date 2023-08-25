package com.example.cleanarchitech_text_0506.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cleanarchitech_text_0506.ditest.hiltmodule.DeviceCommunicateViewModelModule
import com.example.domain.enumclass.DeviceType
import com.example.domain.enumclass.SerialCommunicationMessage
import com.example.domain.usecaseinterface.DeviceSetting
import com.example.domain.usecaseinterface.DeviceSettingSharedPreference
import com.example.domain.usecaseinterface.ResponseDeviceSerialCommunication
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class DeviceSerialCommunicateViewModel @Inject constructor(
    private val responseDeviceSerialCommunication: ResponseDeviceSerialCommunication,
    private val deviceSettingSharedPreference: DeviceSettingSharedPreference,
    @DeviceCommunicateViewModelModule.Bluetooth private val bluetoothDeviceSerialCommunicate: DeviceSetting,
    @DeviceCommunicateViewModelModule.Usb private val usbDeviceSerialCommunicate: DeviceSetting
): ViewModel(), Serializable{
    private var dialogMessage: String = ""
    var dialogMessageProperty: String
        get() = dialogMessage
        set(value) { dialogMessage = value }

    private var owner: LifecycleOwner? = null
    var ownerProperty: LifecycleOwner?
        get() = owner
        set(value) { owner = value }

//    val errorOccur: MutableLiveData<Event<Boolean>>
//        get() = _errorOccur

    val serialCommunicateMessage: MutableLiveData<Event<String?>?>
        get() = responseDeviceSerialCommunication.serialCommunicationMessage

    private val _notExistRegisteredDevice = MutableLiveData<Event<String>>()
    val notExistRegisteredDevice: MutableLiveData<Event<String>> = _notExistRegisteredDevice

    val isCompletePayment: MutableLiveData<Event<Boolean?>?>
        get() = responseDeviceSerialCommunication.isCompletePayment

    fun requestDeviceSerialCommunication(byteArray: ByteArray) {
        when(getCurrentRegisteredDeviceType()){
            DeviceType.BLUETOOTH -> { bluetoothDeviceSerialCommunicate.requestDeviceSerialCommunication(byteArray) }
            DeviceType.USB -> { usbDeviceSerialCommunicate.requestDeviceSerialCommunication(byteArray) }
            else -> { _notExistRegisteredDevice.value = Event(SerialCommunicationMessage.NotExistRegisteredDevice.message) }
        }
    }

    fun init() {
        responseDeviceSerialCommunication.init()
    }

    fun unbindService() {
        when(getCurrentRegisteredDeviceType()){
            DeviceType.BLUETOOTH -> { bluetoothDeviceSerialCommunicate.unBindingService() }
            DeviceType.USB -> { usbDeviceSerialCommunicate.unBindingService() }
        }
    }

    fun getCurrentRegisteredDeviceType(): DeviceType {
        return deviceSettingSharedPreference.getCurrentRegisteredDeviceType().deviceType
    }

}