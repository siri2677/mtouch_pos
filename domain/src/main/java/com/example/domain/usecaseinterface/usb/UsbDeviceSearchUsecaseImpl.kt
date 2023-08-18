package com.example.domain.usecaseinterface.usb

import android.hardware.usb.UsbDevice

interface UsbDeviceSearchUsecaseImpl {
    fun searchUsbDevice(): ArrayList<UsbDevice>
}