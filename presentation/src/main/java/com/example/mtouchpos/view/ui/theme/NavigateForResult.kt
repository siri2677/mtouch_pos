package com.example.mtouchpos.view.ui.theme

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.domain.model.device.DeviceInfo
import com.example.mtouchpos.view.navgraph.NavigationBundleKey
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.navigate
import com.example.mtouchpos.viewmodel.DeviceSettingViewModel
import com.example.mtouchpos.viewmodel.OfflinePaymentViewModel
import com.example.mtouchpos.vo.data.CompletePaymentInfo
import com.example.mtouchpos.vo.type.UseCaseResult

fun UseCaseResult.Error.navigateToErrorDialog(navController: NavController) {
    navController.navigate(
        route = NavigationGraphState.CommonView.ErrorDialog.name,
        bundle = bundleOf(NavigationBundleKey.MESSAGE to this.message),
        navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
    )
}

fun UseCaseResult.Exception.navigateToErrorDialog(navController: NavController) {
    navController.navigate(
        route = NavigationGraphState.CommonView.ErrorDialog.name,
        bundle = bundleOf(NavigationBundleKey.MESSAGE to this.exception.message),
        navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
    )
}

fun CompletePaymentInfo.navigateToCompletePaymentView(navController: NavController) {
    navController.navigate(
        route = NavigationGraphState.CommonView.CompletePayment.name,
        bundle = bundleOf( NavigationBundleKey.RESULT_DATA to this),
        navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(
            NavigationGraphState.HomeView.Home.name, false).build()
    )
}

fun OfflinePaymentViewModel.PaymentProcessState.navigateToDeviceDialog(
    navController: NavController,
    deviceInfo: DeviceInfo?
) {
    when(deviceInfo) {
        is DeviceSettingViewModel.BluetoothDeviceInfo -> {
            navController.navigate(
                route = NavigationGraphState.CreditPaymentView.BluetoothDialog.name,
                bundle = bundleOf(NavigationBundleKey.ITEM_LIST to this),
                navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
            )
        }
        is DeviceSettingViewModel.UsbDeviceInfo -> {
            navController.navigate(
                route = NavigationGraphState.CreditPaymentView.UsbDialog.name,
                bundle = bundleOf(NavigationBundleKey.ITEM_LIST to this),
                navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
            )
        }
        else -> {
            navController.navigate(
                route = NavigationGraphState.CommonView.ErrorDialog.name,
                bundle = bundleOf(NavigationBundleKey.MESSAGE to "장치 등록 후 결제 진행 바시기 바랍니다."),
                navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
            )
        }
    }
}

fun UseCaseResult<String>.observeResultLogin(
    navController: NavController,
    afterProcess: () -> Unit
) {
    when(this) {
        is UseCaseResult.Success -> afterProcess()
        is UseCaseResult.Error -> this.navigateToErrorDialog(navController)
        is UseCaseResult.Exception -> this.navigateToErrorDialog(navController)
        UseCaseResult.Init -> {}
    }
}

fun OfflinePaymentViewModel.PaymentProcessState.observeResultPaymentData(
    navController: NavController,
    deviceInfo: DeviceInfo?
) {
    when (this) {
        is OfflinePaymentViewModel.PaymentProcessState.Error,
        is OfflinePaymentViewModel.PaymentProcessState.Fallback,
        OfflinePaymentViewModel.PaymentProcessState.InsertIC,
        OfflinePaymentViewModel.PaymentProcessState.ReadingIC -> {
            this.navigateToDeviceDialog(navController, deviceInfo)
        }
        is OfflinePaymentViewModel.PaymentProcessState.CompletePayment -> {
            this.data.navigateToCompletePaymentView(navController)
        }
        OfflinePaymentViewModel.PaymentProcessState.Init -> {}
    }
}

fun UseCaseResult<CompletePaymentInfo>.observeCompletePaymentInfo(navController: NavController) {
    when (this) {
        is UseCaseResult.Success -> this.value.navigateToCompletePaymentView(navController)
        is UseCaseResult.Error -> this.navigateToErrorDialog(navController)
        is UseCaseResult.Exception -> this.navigateToErrorDialog(navController)
        UseCaseResult.Init -> {}
    }
}
