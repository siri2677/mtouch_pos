package com.example.mtouchpos.device.usb

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.example.mtouchpos.FlowManager
import com.example.mtouchpos.vo.DeviceList
import com.example.mtouchpos.device.deviceinterface.DeviceScan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UsbDeviceScan(val context: Context): DeviceScan {
//    override val listUpdate = MutableSharedFlow<DeviceList>()

    override fun scan() {
        var usbDeviceArrayList: ArrayList<UsbDevice> = ArrayList()
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList: HashMap<String, UsbDevice> = manager.deviceList
        deviceList.values.forEach { device -> usbDeviceArrayList.add(device) }
        CoroutineScope(Dispatchers.IO).launch { FlowManager.deviceListSharedFlow.emit(
            DeviceList.UsbList(usbDeviceArrayList)) }
    }

    override fun cancel() {}

}