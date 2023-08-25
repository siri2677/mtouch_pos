package com.mtouch.ksr02_03_04_v2.Ui

import android.hardware.usb.UsbDevice
import androidx.lifecycle.*
import com.example.cleanarchitech_text_0506.ditest.hiltmodule.DeviceCommunicateViewModelModule
import com.example.domain.enumclass.DeviceType
import com.example.domain.usecaseinterface.DeviceSetting
import com.example.domain.usecaseinterface.DeviceSettingSharedPreference
import com.example.domain.usecaseinterface.UsbDeviceSearchUsecaseImpl
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class UsbViewModel @Inject constructor(
    private val usbDeviceSearchUsecaseImpl: UsbDeviceSearchUsecaseImpl,
    private val deviceSettingSharedPreference: DeviceSettingSharedPreference,
    @DeviceCommunicateViewModelModule.Usb private val usbDeviceSerialCommunicate: DeviceSetting
) : ViewModel() {
    private val _listUpdate = MutableLiveData<ArrayList<UsbDevice>?>()
    val listUpdate : MutableLiveData<ArrayList<UsbDevice>?>
        get() = _listUpdate

    val permissionCheck : MutableLiveData<Event<Boolean>>?
        get() = usbDeviceSerialCommunicate.permissionCheckComplete

    fun usbDeviceScan(){
        var usbDevices = usbDeviceSearchUsecaseImpl.searchUsbDevice()
        if(usbDevices?.size != 0) {
            _listUpdate.value = usbDevices
        }
    }

    fun usbDeviceConnect(usbDevice: UsbDevice){
        deviceSettingSharedPreference.setCurrentRegisteredDeviceType(DeviceType.USB, usbDevice.toString())
        usbDeviceSerialCommunicate.deviceConnect(usbDevice.toString())
    }

    fun usbDeviceDisconnect(){
        usbDeviceSerialCommunicate.deviceDisConnect()
        deviceSettingSharedPreference.clearCurrentRegisteredDeviceType()
    }

    fun usbDeviceUnBindingService(){
        usbDeviceSerialCommunicate.unBindingService()
    }
}

