package com.example.cleanarchitech_text_0506.scandevice

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.cleanarchitech_text_0506.sealed.DeviceList
import com.example.cleanarchitech_text_0506.deviceinterface.DeviceScan
import com.example.cleanarchitech_text_0506.vo.DomainLayerConstantObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

class BluetoothDeviceScan(val context: Context): DeviceScan {
    var storedScanResults: ArrayList<BluetoothDevice>? = ArrayList()
    private val bleManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bleAdapter: BluetoothAdapter = bleManager.adapter
    override val listUpdate = MutableSharedFlow<DeviceList>()

    override fun scan() {
        if (bleAdapter.isEnabled
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bleAdapter?.bluetoothLeScanner?.startScan(
                List<ScanFilter>(1) {
                    ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid(UUID.fromString(DomainLayerConstantObject.SERVICE_STRING)))
                        .build()
                },
                ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build(),
                BLEScanCallback
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun cancel() {
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bleAdapter?.bluetoothLeScanner?.stopScan(BLEScanCallback)
        }
//        isScanning.postValue(Event(false))
    }

    private val BLEScanCallback: ScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            addScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for(result in results){
                addScanResult(result)
            }
        }

        override fun onScanFailed(_error: Int) {
//            scanBluetoothScanResult.postValue(Event(bluetoothScanResult))
        }
    }

    private fun addScanResult(result: ScanResult) {
        for (storedScanResult in storedScanResults!!) {
            if (storedScanResult.address == result.device.address) return
        }
        storedScanResults?.add(result.device)
        CoroutineScope(Dispatchers.IO).launch{
            listUpdate.emit(DeviceList.BluetoothList(storedScanResults!!))
        }
//        scanBluetoothScanResult.postValue(Event(bluetoothScanResult))
//        listUpdate.value = Event(storedScanResults)
    }
}