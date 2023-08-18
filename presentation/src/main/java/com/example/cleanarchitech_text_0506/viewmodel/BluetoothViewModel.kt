package com.example.cleanarchitech_text_0506.viewmodel

import android.bluetooth.*
import android.content.Context
import androidx.lifecycle.*
import com.example.domain.model.BluetoothScanResult
import com.example.domain.usecaseinterface.RequestDeviceSerialCommunication
import com.example.domain.usecaseinterface.bluetooth.BluetoothDeviceScanUsecaseImpl
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event
import java.util.*
import javax.inject.Inject

class BluetoothViewModel @Inject constructor(
    private var requestDeviceSerialCommunication: RequestDeviceSerialCommunication,
    private var bluetoothDeviceScanUsecaseImpl: BluetoothDeviceScanUsecaseImpl,
//    private var bluetoothDeviceConnectUseCaseImpl: BluetoothDeviceConnectUseCaseImpl,
//    private var bluetoothDeviceSerialCommunicationUseCaseImpl: BluetoothDeviceSerialCommunicationUseCaseImpl,
//    private var usbDeviceConnectUsecaseImpl: UsbDeviceConnectUsecaseImpl,
//    private var usbDeviceSerialCommunicationUseCaseImpl: UsbDeviceSerialCommunicationUsecaseImpl,
) : ViewModel() {
    val listUpdate : MutableLiveData<Event<ArrayList<BluetoothDevice>?>>
        get() = bluetoothDeviceScanUsecaseImpl.listUpdate

    private val _requestEnableBLE = MutableLiveData<Event<BluetoothScanResult>>()
    val requestEnableBLE : MutableLiveData<Event<BluetoothScanResult>?>
        get() = bluetoothDeviceScanUsecaseImpl.scanBluetoothScanResult

    val isFirstConnectComplete : MutableLiveData<Event<Boolean>>?
        get() = requestDeviceSerialCommunication.getDeviceType()?.isFirstConnectComplete


    fun scanBluetoothDevice() {
        bluetoothDeviceScanUsecaseImpl.startScan()
    }

    fun stopScanBluetoothDevice() {
        bluetoothDeviceScanUsecaseImpl.stopScan()
    }

    fun bluetoothDeviceConnect(bluetoothDevice: BluetoothDevice){
        requestDeviceSerialCommunication.setBluetoothDevice(bluetoothDevice.address)
        requestDeviceSerialCommunication.getDeviceType()?.deviceConnect(bluetoothDevice.address)
    }

    fun bluetoothDeviceDisConnect(){
        requestDeviceSerialCommunication.getDeviceType()?.deviceDisConnect()
        requestDeviceSerialCommunication.deleteCurrentRegisteredDeviceType()
    }

    fun bluetoothDeviceResister(bluetoothDevice: BluetoothDevice) {
        requestDeviceSerialCommunication.setBluetoothDevice(bluetoothDevice.address)
    }

    fun bluetoothDeviceUnResister() {
        requestDeviceSerialCommunication.deleteCurrentRegisteredDeviceType()
    }

    fun bluetoothDeviceUnBinding(){
        requestDeviceSerialCommunication.getDeviceType()?.unBindingService()
    }


//        bluetoothDeviceSerialCommunicationUseCaseImpl.requestBluetoothDeviceSerialCommunication(byteArray)
//        BluetoothDeviceCommunicate(context).communicate(byteArray)
//        BluetoothDeviceCommunicate(context).connectDevice2()

}