package com.example.mtouchpos.viewmodel.usecasemanager.reader.usb

import android.content.Context
import android.hardware.usb.UsbManager
import com.example.domain.usecase.device.manager.SearchDeviceManager
import com.example.domain.model.device.DeviceInfo
import com.example.mtouchpos.viewmodel.DeviceSettingViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class SearchUsb(val context: Context) : SearchDeviceManager {
    override val deviceList: MutableStateFlow<List<DeviceInfo>> = MutableStateFlow(emptyList())
    override fun scan() {
        deviceList.value = (context.getSystemService(Context.USB_SERVICE) as UsbManager).deviceList.values.map {
            DeviceSettingViewModel.UsbDeviceInfo(
                deviceName = it.deviceName,
                productName = it.productName!!,
                deviceInformation = it.toString()
            )
        }
    }

    override fun cancel() {}
}