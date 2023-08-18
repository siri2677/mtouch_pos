package com.mtouch.ksr02_03_04_v2.Ui

import android.hardware.usb.UsbDevice
import android.util.Log
import androidx.lifecycle.*
import com.example.domain.usecaseinterface.RequestDeviceSerialCommunication
import com.example.domain.usecaseinterface.usb.UsbDeviceSearchUsecaseImpl
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event
import java.util.ArrayList
import javax.inject.Inject

class UsbViewModel @Inject constructor(
    private val requestDeviceSerialCommunication: RequestDeviceSerialCommunication,
    private val usbDeviceSearchUsecaseImpl: UsbDeviceSearchUsecaseImpl,
//    private val usbDeviceSerialCommunicationUsecaseImpl: UsbDeviceSerialCommunicationUsecaseImpl,
//    private val bluetoothDeviceSerialCommunicationUseCaseImpl: BluetoothDeviceSerialCommunicationUseCaseImpl
) : ViewModel() {
//    val statusTxt: LiveData<String>
//        get() = usbRepository.usbStatusMessage().asLiveData(viewModelScope.coroutineContext)

    private val _listUpdate = MutableLiveData<ArrayList<UsbDevice>?>()
    val listUpdate : MutableLiveData<ArrayList<UsbDevice>?>
        get() = _listUpdate

    val permissionCheck : MutableLiveData<Event<Boolean>>?
        get() = requestDeviceSerialCommunication.getDeviceType()?.permissionCheckComplete


    fun usbDeviceScan(){
        var usbDevices = usbDeviceSearchUsecaseImpl.searchUsbDevice()
        if(usbDevices?.size != 0) {
            _listUpdate.value = usbDevices
        }
    }

    fun usbDeviceConnect(usbDevice: UsbDevice){
//        bluetoothDeviceSerialCommunicationUseCaseImpl.unBindingBluetoothConnectService()
//        bluetoothDeviceConnectUseCaseImpl.bluetoothDeviceDisConnect()
//        usbDeviceConnectUsecaseImpl.usbDeviceConnect(usbDevice)
        requestDeviceSerialCommunication.setUsbDevice(usbDevice)
        Log.w("getDeviceSetting", requestDeviceSerialCommunication.getDeviceType().toString())
        requestDeviceSerialCommunication.getDeviceType()?.deviceConnect(usbDevice.toString())
    }

    fun usbDeviceDisconnect(){
        requestDeviceSerialCommunication.getDeviceType()?.deviceDisConnect()
        requestDeviceSerialCommunication.deleteCurrentRegisteredDeviceType()
//        usbDeviceSerialCommunicationUsecaseImpl.unBindingUsbConnectService()
    }

    fun usbDeviceUnBindingService(){
        Log.w("usbDeviceService", "usbDeviceUnBindingService")
        Log.w("getDeviceSetting", requestDeviceSerialCommunication.getDeviceType().toString())
        requestDeviceSerialCommunication.getDeviceType()?.unBindingService()
//        usbDeviceSerialCommunicationUsecaseImpl.unBindingUsbConnectService()
    }


}

