package com.example.mtouchpos.view.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.mtouchpos.view.util.LoadingDialogContent
import com.example.mtouchpos.view.util.MessageDialogContent
import com.example.mtouchpos.viewmodel.BluetoothCardReaderSettingVM
import com.example.mtouchpos.viewmodel.UsbCardReaderSettingVM
import com.example.mtouchpos.vo.info.PaymentProcessState

@Composable
fun CardReaderConnectDialog(
    navController: NavController,
    bluetoothCardReaderSettingVM: BluetoothCardReaderSettingVM
) {
    val deviceConnectState = bluetoothCardReaderSettingVM.deviceConnectState
        .collectAsStateWithLifecycle(PaymentProcessState.CommunicateCardReader.Loading).value

    LaunchedEffect(Unit) {
        if(deviceConnectState is PaymentProcessState.CommunicateCardReader.Loading) {
            bluetoothCardReaderSettingVM.connect()
        }
    }

    Dialog(
        onDismissRequest = {
            bluetoothCardReaderSettingVM.init()
            navController.popBackStack()
        }
    ) {
        CardReaderConnectDialog(
            navController = navController,
            paymentProcessState = deviceConnectState
        )
    }
}

@Composable
fun CardReaderConnectDialog(
    navController: NavController,
    usbCardReaderSettingVM: UsbCardReaderSettingVM
) {
    val deviceConnectState = usbCardReaderSettingVM.deviceConnectState
        .collectAsStateWithLifecycle(PaymentProcessState.CommunicateCardReader.Loading).value

    LaunchedEffect(Unit) {
        if(deviceConnectState is PaymentProcessState.CommunicateCardReader.Loading) {
            usbCardReaderSettingVM.connect()
        }
    }

    Dialog(
        onDismissRequest = {
            usbCardReaderSettingVM.init()
            navController.popBackStack()
        }
    ) {
        CardReaderConnectDialog(
            navController = navController,
            paymentProcessState = deviceConnectState
        )
    }
}

@Composable
fun CardReaderConnectDialog(
    navController: NavController,
    paymentProcessState: PaymentProcessState.CommunicateCardReader,
) {
    when(paymentProcessState) {
        is PaymentProcessState.CommunicateCardReader.Inactive -> {
            MessageDialogContent(
                navController = navController,
                message = "연결 테스트에 성공하였습니다."
            )
        }

        is PaymentProcessState.CommunicateCardReader.Error -> {
            MessageDialogContent(
                navController = navController,
                message = paymentProcessState.message
            )
        }

        is PaymentProcessState.CommunicateCardReader.Loading -> {
            LoadingDialogContent()
        }

        else -> {
            BluetoothCommunicateContent(paymentProcessState)
        }
    }
}