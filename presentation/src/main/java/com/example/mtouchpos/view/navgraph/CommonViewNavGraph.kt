package com.example.mtouchpos.view.navgraph

import android.os.Build
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.example.mtouchpos.view.ui.BluetoothDevicePaymentDialog
import com.example.mtouchpos.view.ui.CompletePaymentPage
import com.example.mtouchpos.view.ui.UsbDevicePaymentDialog
import com.example.mtouchpos.view.util.ErrorDialog
import com.example.mtouchpos.view.util.ItemListDialog
import com.example.mtouchpos.view.util.LoadingDialog
import com.example.mtouchpos.view.ui.theme.NavigationBundleKey
import com.google.accompanist.pager.ExperimentalPagerApi
import java.io.Serializable

class CommonViewNavGraph(
    val navController: NavController,
    val navGraphBuilder: NavGraphBuilder
) {
    inline fun <reified T : Serializable> NavBackStackEntry.getSerializableArgument(
        key: String
    ): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(key, T::class.java)
        } else {
            arguments?.getSerializable(key) as T
        }
    }

    fun itemListDialog() {
        navGraphBuilder.dialog(NavigationGraphState.CommonView.ItemListDialog.name) { backStackEntry ->
            ItemListDialog(
                navController = navController,
                itemListStatus = backStackEntry.getSerializableArgument(NavigationBundleKey.ItemList)!!
            )
        }
    }

    fun errorDialog() {
        navGraphBuilder.dialog(NavigationGraphState.CommonView.ErrorDialog.name) { backStackEntry ->
            ErrorDialog(
                navController = navController,
                message = backStackEntry.getSerializableArgument(NavigationBundleKey.message)!!,
//                onDismissRequest = backStackEntry.getSerializableArgument(NavigationBundleKey.onDismiss)
            )
        }
    }

    fun completePaymentPage() {
        navGraphBuilder.run {
            composable(NavigationGraphState.CommonView.CompletePayment.name) { backStackEntry ->
                CompletePaymentPage(
                    navController = navController,
                    completePayment = backStackEntry.getSerializableArgument(
                        NavigationBundleKey.resultData
                    )!!
                )
            }
            dialog(NavigationGraphState.CreditPaymentView.BluetoothPaymentDialog.name) { backStackEntry ->
                BluetoothDevicePaymentDialog(
                    navController = navController,
                    deviceSerialCommunicate = backStackEntry.getSerializableArgument(
                        NavigationBundleKey.ItemList
                    )!!
                )
            }
            dialog(NavigationGraphState.CreditPaymentView.UsbPaymentDialog.name) { backStackEntry ->
                UsbDevicePaymentDialog(
                    navController = navController,
                    deviceSerialCommunicate = backStackEntry.getSerializableArgument(
                        NavigationBundleKey.ItemList
                    )!!
                )
            }
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    fun loadingDialog() {
        navGraphBuilder.dialog(NavigationGraphState.CommonView.LoadingDialog.name) { backStackEntry ->
            LoadingDialog(
                navController = navController,
                paymentHistoryViewModel = backStackEntry.getSerializableArgument(NavigationBundleKey.responseTmsAPI)!!,
            )
        }
    }
}