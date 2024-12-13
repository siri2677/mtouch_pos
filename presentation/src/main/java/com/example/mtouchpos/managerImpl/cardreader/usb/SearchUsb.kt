package com.example.mtouchpos.managerImpl.cardreader.usb

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.example.domain.manager.cardreader.CardReaderSearchManager
import com.example.domain.model.cardreader.CardReaderData
import com.example.mtouchpos.viewmodel.CardReaderConnectVM
import kotlinx.coroutines.flow.MutableStateFlow

class SearchUsb(val context: Context) : CardReaderSearchManager {
    override val deviceList: MutableStateFlow<List<CardReaderData>> = MutableStateFlow(emptyList())
    override fun scan() {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        deviceList.value = manager.getDeviceList().values.map { it ->
            CardReaderConnectVM.UsbDeviceInfo(
                deviceName = it.deviceName,
                productName = it.productName!!,
                deviceInformation = it.toString()
            )
        }
    }

    override fun cancel() {}
}