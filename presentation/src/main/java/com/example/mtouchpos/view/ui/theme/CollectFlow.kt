package com.example.mtouchpos.view.ui.theme

import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.mtouchpos.FlowManager
import com.example.mtouchpos.vo.DeviceSerialCommunicate
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.navigate
import com.example.mtouchpos.viewmodel.DeviceCommunicationViewModel
import com.example.mtouchpos.viewmodel.MainActivityViewModel
import com.example.domain.vo.DeviceType
import com.example.mtouchpos.dto.ResponseFlowData


@Composable
fun ObserveResultLogin(
    navController: NavController,
    mainActivityViewModel: MainActivityViewModel,
    afterProcess: () -> Unit
) {
    when(val responseFlowData = mainActivityViewModel.responseFlowData.collectAsStateWithLifecycle(initialValue = "").value) {
        is ResponseFlowData.CompleteLogin -> {
            afterProcess()
        }
        is ResponseFlowData.Error -> {
            navController.navigate(
                route = NavigationGraphState.CommonView.ErrorDialog.name,
                bundle = bundleOf(NavigationBundleKey.message to responseFlowData.message),
                navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(
                    NavigationGraphState.HomeView.Login.name, false).build()
            )
        }
    }
}

@Composable
fun ObserveResultPayment(
    responseFlowData: ResponseFlowData,
    navController: NavController,
    responsePage: String
) {
    when (responseFlowData) {
        is ResponseFlowData.CompletePayment -> {
            navController.navigate(
                route = NavigationGraphState.CommonView.CompletePayment.name,
                bundle = bundleOf( NavigationBundleKey.resultData to responseFlowData),
                navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(
                    NavigationGraphState.HomeView.Home.name, false).build()
            )
        }
        is ResponseFlowData.Error -> {
            navController.navigate(
                route = NavigationGraphState.CommonView.ErrorDialog.name,
                bundle = bundleOf(NavigationBundleKey.message to responseFlowData.message),
                navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(
                    responsePage, false).build()
            )
        }
        else -> {}
    }
}

@Composable
fun ObserveResultSerialCommunicate(
    deviceCommunicationViewModel: DeviceCommunicationViewModel,
    navController: NavController
) {
    val deviceSerialCommunicate =
        FlowManager.deviceSerialCommunicate.collectAsStateWithLifecycle(
            DeviceSerialCommunicate.Init).value

    if (deviceSerialCommunicate is DeviceSerialCommunicate.SerialCommunicationMessage) {
        when (deviceCommunicationViewModel.getCurrentRegisteredDeviceType()) {
            DeviceType.Bluetooth -> {
                navController.navigate(
                    route = NavigationGraphState.CreditPaymentView.BluetoothPaymentDialog.name,
                    bundle = bundleOf(NavigationBundleKey.ItemList to deviceSerialCommunicate),
                    navOptions = NavOptions.Builder().setLaunchSingleTop(true).build(),
                )
            }
            DeviceType.Usb -> {
                navController.navigate(
                    route = NavigationGraphState.CreditPaymentView.UsbPaymentDialog.name,
                    bundle = bundleOf(NavigationBundleKey.ItemList to deviceSerialCommunicate),
                    navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                )
            }
            else -> {}
        }
    }
}
