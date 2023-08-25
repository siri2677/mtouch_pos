package com.example.domain.usecaseinterface

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import com.example.domain.model.BluetoothScanResult
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event
import kotlinx.coroutines.flow.MutableStateFlow

interface BluetoothDeviceScanUsecaseImpl {
    val scanBluetoothScanResult: MutableLiveData<Event<BluetoothScanResult>?>
    val listUpdate: MutableLiveData<Event<ArrayList<BluetoothDevice>?>>
    fun startScan()
    fun stopScan()

}