package com.example.mtouchpos.managerImpl.cardreader.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.domain.manager.cardreader.CardReaderSearchManager
import com.example.domain.model.cardreader.CardReaderData
import com.example.mtouchpos.service.BluetoothService.Companion.SERVICE_STRING
import com.example.mtouchpos.viewmodel.CardReaderConnectVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class SearchBluetooth(val context: Context): CardReaderSearchManager {
    override val deviceList: MutableStateFlow<List<CardReaderData>> = MutableStateFlow(emptyList())
    private val bleManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bleAdapter: BluetoothAdapter = bleManager.adapter
    private val bleScanCallback: ScanCallback =
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP) object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            addScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for(result in results){
                addScanResult(result)
            }
        }

        override fun onScanFailed(error: Int) {}
    }

    override fun scan() {
        if (bleAdapter.isEnabled
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bleAdapter?.bluetoothLeScanner?.startScan(
                List<ScanFilter>(1) {
                    ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid(UUID.fromString(SERVICE_STRING)))
                        .build()
                },
                ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build(),
                bleScanCallback
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun cancel() {
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bleAdapter?.bluetoothLeScanner?.stopScan(bleScanCallback)
        }
    }

    @SuppressLint("MissingPermission")
    private fun addScanResult(result: ScanResult) {
        if (deviceList.value.none { it.deviceInformation == result.device.address }) {
            val bluetoothDeviceInfo = CardReaderConnectVM.BluetoothDeviceInfo(
                deviceName = result.device.name,
                deviceInformation = result.device.address
            )

            CoroutineScope(Dispatchers.IO).launch {
                deviceList.update {
                    deviceList.value.toMutableList().apply { add(bluetoothDeviceInfo) }
                }
            }
        }
    }
}
