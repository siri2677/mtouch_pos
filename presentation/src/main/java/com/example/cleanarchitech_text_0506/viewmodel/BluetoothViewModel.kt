package com.example.cleanarchitech_text_0506.viewmodel

import android.bluetooth.*
import androidx.lifecycle.*
import com.example.cleanarchitech_text_0506.ditest.hiltmodule.DeviceCommunicateViewModelModule
import com.example.domain.enumclass.DeviceType
import com.example.domain.model.BluetoothScanResult
import com.example.domain.usecaseinterface.DeviceSetting
import com.example.domain.usecaseinterface.DeviceSettingSharedPreference
import com.example.domain.usecaseinterface.BluetoothDeviceScanUsecaseImpl
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private var bluetoothDeviceScanUsecaseImpl: BluetoothDeviceScanUsecaseImpl,
    private val deviceSettingSharedPreference: DeviceSettingSharedPreference,
    @DeviceCommunicateViewModelModule.Bluetooth private val bluetoothDeviceSerialCommunicate: DeviceSetting
) : ViewModel() {
    val listUpdate : MutableLiveData<Event<ArrayList<BluetoothDevice>?>>
        get() = bluetoothDeviceScanUsecaseImpl.listUpdate

    private val _requestEnableBLE = MutableLiveData<Event<BluetoothScanResult>>()
    val requestEnableBLE : MutableLiveData<Event<BluetoothScanResult>?>
        get() = bluetoothDeviceScanUsecaseImpl.scanBluetoothScanResult

    val isFirstConnectComplete : MutableLiveData<Event<Boolean>>?
        get() = bluetoothDeviceSerialCommunicate.isFirstConnectComplete


    fun scanBluetoothDevice() {
        bluetoothDeviceScanUsecaseImpl.startScan()
    }

    fun stopScanBluetoothDevice() {
        bluetoothDeviceScanUsecaseImpl.stopScan()
    }

    fun bluetoothDeviceConnect(bluetoothDevice: BluetoothDevice){
        deviceSettingSharedPreference.setCurrentRegisteredDeviceType(DeviceType.BLUETOOTH, bluetoothDevice.address)
        bluetoothDeviceSerialCommunicate.deviceConnect(bluetoothDevice.address)
    }

    fun bluetoothDeviceDisConnect(){
        deviceSettingSharedPreference.clearCurrentRegisteredDeviceType()
        bluetoothDeviceSerialCommunicate.deviceDisConnect()
    }

    fun bluetoothDeviceResister(bluetoothDevice: BluetoothDevice) {
        deviceSettingSharedPreference.setCurrentRegisteredDeviceType(DeviceType.BLUETOOTH, bluetoothDevice.address)
    }

    fun bluetoothDeviceUnResister() {
        deviceSettingSharedPreference.clearCurrentRegisteredDeviceType()
    }

    fun bluetoothDeviceUnBinding(){
        bluetoothDeviceSerialCommunicate.unBindingService()
    }
}