package com.kwonps.mtouchpos

import android.content.Context
import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.usecase.cardreader.UpdateConnectedDeviceInfo
import com.kwonps.mtouchpos.managerImpl.cardreader.bluetooth.ConnectBluetooth
import com.kwonps.mtouchpos.viewmodel.UsbCardReaderSettingVM
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

    private lateinit var deviceSettingViewModel: UsbCardReaderSettingVM
    private lateinit var context: Context
    private lateinit var searchBluetoothDeviceUseCase: SearchDevice
    private lateinit var searchUsbDeviceUseCase: SearchUsbDevice
    private lateinit var deviceConnectUseCase: ConnectCardReader
    private lateinit var updateConnectedDeviceInfoUseCase: UpdateConnectedDeviceInfo

    private val bluetoothDeviceInfo = UsbCardReaderSettingVM.BluetoothDeviceInfo(
        deviceInformation = "f0:00:00:00:00:00",
        deviceName = "ksr03"
    )

    private val usbDeviceInfo = UsbCardReaderSettingVM.UsbDeviceInfo(
        deviceInformation = "/dev/bus/usb/001/003",
        deviceName = "1027",
        productName = "24577"
    )

    @Before
    fun setup() {
        searchBluetoothDeviceUseCase = mockk<SearchDevice>()
        searchUsbDeviceUseCase = mockk<SearchUsbDevice>()
        deviceConnectUseCase = mockk<ConnectCardReader>()
        updateConnectedDeviceInfoUseCase = mockk<UpdateConnectedDeviceInfo>()
        context = mockk<Context>()
        deviceSettingViewModel = UsbCardReaderSettingVM(
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
                is UsbCardReaderSettingVM.BluetoothDeviceInfo -> {
                    assertEquals(deviceInformation, bluetoothDeviceInfo.deviceInformation)
                    assertEquals(deviceName, bluetoothDeviceInfo.deviceName)
                }
                is UsbCardReaderSettingVM.UsbDeviceInfo -> {
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
        val deviceConnectStatus = CardReaderStatus.Connected
        val deviceConnectState = UsbCardReaderSettingVM.DeviceConnectState.Connected

        coEvery { deviceConnectUseCase(any(), any()) } returns flow { emit(deviceConnectStatus) }

        deviceSettingViewModel.connectDevice(ConnectBluetooth(context))

        coVerify(exactly = 1) { deviceConnectUseCase(any(), any()) }

        assertEquals(deviceSettingViewModel.deviceConnectStatus.value, deviceConnectState)
    }
}