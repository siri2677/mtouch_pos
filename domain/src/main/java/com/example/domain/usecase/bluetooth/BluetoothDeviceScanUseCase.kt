package com.example.domain.usecase.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.example.domain.model.BluetoothScanResult
import com.example.domain.model.DomainLayerConstantObject.Companion.SERVICE_STRING
import com.example.domain.usecaseinterface.bluetooth.BluetoothDeviceScanUsecaseImpl
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event
import java.util.UUID
import javax.inject.Inject

class BluetoothDeviceScanUseCase @Inject constructor(context: Context)
    : BluetoothDeviceScanUsecaseImpl {

    val context: Context = context
    var storedScanResults: ArrayList<BluetoothDevice>? = ArrayList()
    val bleManager: BluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    val bleAdapter: BluetoothAdapter? = bleManager.adapter

    override val scanBluetoothScanResult = MutableLiveData<Event<BluetoothScanResult>?>()
    override val listUpdate = MutableLiveData<Event<ArrayList<BluetoothDevice>?>>()

    override fun startScan() {
        val bluetoothScanResult = BluetoothScanResult()
        if (bleAdapter == null || !bleAdapter?.isEnabled!!) {
            bluetoothScanResult?.resultMessage = "주위에 블루투스 기기가 탐색되지 않습니다"
            bluetoothScanResult?.isScanning = false
            bluetoothScanResult?.isStatusChange = true
        } else {
            val filters: MutableList<ScanFilter> = ArrayList()
            val scanFilter: ScanFilter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(UUID.fromString(SERVICE_STRING)))
                .build()
            filters.add(scanFilter)

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bleAdapter?.bluetoothLeScanner?.startScan(filters, settings, BLEScanCallback)
            }

            bluetoothScanResult?.resultMessage = "블루투스 기기 탐색중입니다"
            bluetoothScanResult?.isStatusChange = true
//            isScanning.postValue(Event(true))
        }
        scanBluetoothScanResult.postValue(Event(bluetoothScanResult))
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun stopScan() {
        val bluetoothScanResult = BluetoothScanResult()
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bleAdapter?.bluetoothLeScanner?.stopScan(BLEScanCallback)
        }
        bluetoothScanResult?.resultMessage = "블루투스 탐색을 종료합니다"
        bluetoothScanResult?.isStatusChange = true
//        isScanning.postValue(Event(false))
        scanBluetoothScanResult.postValue(Event(bluetoothScanResult))
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
            val bluetoothScanResult = BluetoothScanResult()
            bluetoothScanResult?.resultMessage = "블루투스 탐색을 종료합니다"
            bluetoothScanResult?.isStatusChange = true
            scanBluetoothScanResult.postValue(Event(bluetoothScanResult))
        }
    }

    private fun addScanResult(result: ScanResult) {
        val currentScanResultDeviceAddress = result.device.address
        for (storedScanResult in storedScanResults!!) {
            if (storedScanResult.address == currentScanResultDeviceAddress) return
        }
        storedScanResults?.add(result.device)
        val bluetoothScanResult = BluetoothScanResult()
        bluetoothScanResult?.resultMessage = "블루투스 기기가 탐색되었습니다 기기명: $result."
        bluetoothScanResult?.isStatusChange = true
        Log.w("bluetoothDevice", storedScanResults.toString())
        scanBluetoothScanResult.postValue(Event(bluetoothScanResult))
        listUpdate.value = Event(storedScanResults)
    }
}