package com.example.domain.usecaseinterface

import android.hardware.usb.UsbDevice

interface UsbDeviceSearchUsecaseImpl {
    fun searchUsbDevice(): ArrayList<UsbDevice>
}