package com.example.cleanarchitech_text_0506.scandevice

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.example.cleanarchitech_text_0506.sealed.DeviceList
import com.example.cleanarchitech_text_0506.deviceinterface.DeviceScan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class UsbDeviceScan(val context: Context): DeviceScan {
    override val listUpdate = MutableSharedFlow<DeviceList>()

    override fun scan() {
        var usbDeviceArrayList: ArrayList<UsbDevice> = ArrayList()
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList: HashMap<String, UsbDevice> = manager.deviceList
        deviceList.values.forEach { device -> usbDeviceArrayList.add(device) }
        CoroutineScope(Dispatchers.IO).launch { listUpdate.emit(DeviceList.USBList(usbDeviceArrayList)) }
    }

    override fun cancel() {}
}