package com.example.domain.usecase

import android.content.Context
import android.hardware.usb.UsbDevice
import androidx.lifecycle.MutableLiveData
import com.example.domain.enumclass.DeviceSharedPreferenceKey
import com.example.domain.enumclass.DeviceType
import com.example.domain.enumclass.SerialCommunicationMessage
import com.example.domain.usecaseinterface.DeviceSetting
import com.example.domain.usecaseinterface.RequestDeviceSerialCommunication
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event
import javax.inject.Inject
import javax.inject.Named

class RequestDeviceSerialCommunicateImpl @Inject constructor(
    private val context: Context,
    @Named("Bluetooth") private val bluetoothDeviceSerialCommunicate: DeviceSetting,
    @Named("Usb") private val usbDeviceSerialCommunicate: DeviceSetting,
): RequestDeviceSerialCommunication {
    override val errorOccur = MutableLiveData<Event<Boolean>>()
    override val notExistRegisteredDevice = MutableLiveData<Event<String>>()

    override fun setBluetoothDevice(bluetoothAddress: String) {
        DeviceSettingSharedPreferenceImpl(context).setBluetoothDeviceInformation(bluetoothAddress)
        DeviceSettingSharedPreferenceImpl(context).setKeepBluetoothConnection(false)
    }

    override fun setUsbDevice(usbDevice: UsbDevice) {
        DeviceSettingSharedPreferenceImpl(context).setUsbDeviceInformation(usbDevice.toString())
    }

    override fun deleteCurrentRegisteredDeviceType() {
        DeviceSettingSharedPreferenceImpl(context).deleteCurrentRegisteredDeviceType()
    }

    override fun getCurrentRegisteredDeviceType(): String {
        return DeviceSettingSharedPreferenceImpl(context).getCurrentRegisteredDeviceType()
    }

    private fun isExistRegisteredDevice(): Boolean {
        return DeviceSettingSharedPreferenceImpl(context).getCurrentRegisteredDeviceType() != DeviceSharedPreferenceKey.EMPTY_STRING.toString()
    }

    override fun getDeviceType(): DeviceSetting?  {
        return if(isExistRegisteredDevice()) {
            when (DeviceSettingSharedPreferenceImpl(context).getCurrentRegisteredDeviceType()) {
                DeviceType.BLUETOOTH.name -> bluetoothDeviceSerialCommunicate
                DeviceType.USB.name -> usbDeviceSerialCommunicate
                else -> {
                    errorOccur.value = Event(true)
                    null
                }
            }
        } else {
            notExistRegisteredDevice.value = Event(SerialCommunicationMessage.notExistRegisteredDevice.message)
            null
        }
    }
}