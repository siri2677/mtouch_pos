package com.example.mtouchpos

import android.content.Context
import com.example.domain.model.device.DeviceConnectStatus
import com.example.domain.usecase.device.SearchBluetoothDevice
import com.example.domain.usecase.device.ConnectCardReader
import com.example.domain.usecase.device.UpdateConnectedDeviceInfo
import com.example.domain.usecase.device.SearchUsbDevice
import com.example.mtouchpos.viewmodel.usecasemanager.reader.bluetooth.ConnectBluetooth
import com.example.mtouchpos.viewmodel.DeviceSettingViewModel
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DeviceSettingViewModelTest {
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var deviceSettingViewModel: DeviceSettingViewModel
    private lateinit var context: Context
    private lateinit var searchBluetoothDeviceUseCase: SearchBluetoothDevice
    private lateinit var searchUsbDeviceUseCase: SearchUsbDevice
    private lateinit var deviceConnectUseCase: ConnectCardReader
    private lateinit var updateConnectedDeviceInfoUseCase: UpdateConnectedDeviceInfo

    private val bluetoothDeviceInfo = DeviceSettingViewModel.BluetoothDeviceInfo(
        deviceInformation = "f0:00:00:00:00:00",
        deviceName = "ksr03"
    )

    private val usbDeviceInfo = DeviceSettingViewModel.UsbDeviceInfo(
        deviceInformation = "/dev/bus/usb/001/003",
        deviceName = "1027",
        productName = "24577"
    )

    @Before
    fun setup() {
        searchBluetoothDeviceUseCase = mockk<SearchBluetoothDevice>()
        searchUsbDeviceUseCase = mockk<SearchUsbDevice>()
        deviceConnectUseCase = mockk<ConnectCardReader>()
        updateConnectedDeviceInfoUseCase = mockk<UpdateConnectedDeviceInfo>()
        context = mockk<Context>()
        deviceSettingViewModel = DeviceSettingViewModel(
            searchBluetoothDeviceUseCase = searchBluetoothDeviceUseCase,
            searchUsbDeviceUseCase = searchUsbDeviceUseCase,
            connectDeviceUseCase = deviceConnectUseCase,
            updateConnectedDeviceInfoUseCase = updateConnectedDeviceInfoUseCase,
        )
    }

    @Test
    fun `updateDeviceInfo correctly save connectedDeviceInfo result`() = runTest {
        coEvery { updateConnectedDeviceInfoUseCase(any()) } just Runs

        deviceSettingViewModel.updateConnectedDeviceInfo(bluetoothDeviceInfo)

        coVerify(exactly = 1) { updateConnectedDeviceInfoUseCase(any()) }

        with(deviceSettingViewModel.deviceInfo.value) {
            when(this) {
                is DeviceSettingViewModel.BluetoothDeviceInfo -> {
                    assertEquals(deviceInformation, bluetoothDeviceInfo.deviceInformation)
                    assertEquals(deviceName, bluetoothDeviceInfo.deviceName)
                }
                is DeviceSettingViewModel.UsbDeviceInfo -> {
                    assertEquals(deviceInformation, usbDeviceInfo.deviceInformation)
                    assertEquals(deviceName, usbDeviceInfo.deviceName)
                    assertEquals(productName, usbDeviceInfo.productName)
                }
            }
        }
    }

    @Test
    fun `searchBluetoothDevice success emits bluetoothDeviceInfo result`() = runTest {
        coEvery { searchBluetoothDeviceUseCase.scan() } returns flow { emit(listOf(bluetoothDeviceInfo)) }

        deviceSettingViewModel.searchBluetoothDevice()

        coVerify(exactly = 1) { searchBluetoothDeviceUseCase.scan() }

        assertEquals(deviceSettingViewModel.deviceInfoList.value, listOf(bluetoothDeviceInfo))
    }

    @Test
    fun `searchUsbDevice success emits usbDeviceInfo result`() = runTest {
        coEvery { searchUsbDeviceUseCase() } returns listOf(usbDeviceInfo)

        deviceSettingViewModel.searchUsbDevice()

        coVerify(exactly = 1) { searchUsbDeviceUseCase() }

        assertEquals(deviceSettingViewModel.deviceInfoList.value, listOf(usbDeviceInfo))
    }

    @Test
    fun `deviceConnect success emits deviceConnectState result`() = runTest  {
        val deviceConnectStatus = DeviceConnectStatus.ConnectComplete
        val deviceConnectState = DeviceSettingViewModel.DeviceConnectState.Connected

        coEvery { deviceConnectUseCase(any(), any()) } returns flow { emit(deviceConnectStatus) }

        deviceSettingViewModel.connectDevice(ConnectBluetooth(context))

        coVerify(exactly = 1) { deviceConnectUseCase(any(), any()) }

        assertEquals(deviceSettingViewModel.deviceConnectStatus.value, deviceConnectState)
    }
}