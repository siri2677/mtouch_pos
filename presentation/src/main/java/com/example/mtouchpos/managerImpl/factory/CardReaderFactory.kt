package com.example.mtouchpos.managerImpl.factory

import android.content.Context
import com.example.domain.manager.cardreader.CardReaderConnectManager
import com.example.mtouchpos.managerImpl.cardreader.bluetooth.ConnectBluetooth
import com.example.mtouchpos.managerImpl.cardreader.usb.ConnectUsb
import com.example.mtouchpos.viewmodel.CardReaderConnectVM

class CardReaderFactory(private val context: Context) {
    fun getConnectManger(deviceType: CardReaderConnectVM.DeviceType): CardReaderConnectManager =
        when(deviceType) {
            CardReaderConnectVM.DeviceType.Bluetooth -> {
                ConnectBluetooth(context)
            }
            CardReaderConnectVM.DeviceType.Usb -> {
                ConnectUsb(context)
            }
        }
}