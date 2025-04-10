package com.example.mtouchpos.view.navgraph

import android.os.Build
import android.util.Log
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.example.mtouchpos.view.sharedViewModel
import com.example.mtouchpos.view.ui.CompletePaymentView
import com.example.mtouchpos.view.ui.PaymentProcessDialog
import com.example.mtouchpos.view.util.MessageDialog
import com.example.mtouchpos.view.util.ItemListDialog
import com.example.mtouchpos.viewmodel.OfflinePaymentVM
import java.io.Serializable

open class CommonViewNavGraph(
    open val navController: NavController,
    open val navGraphBuilder: NavGraphBuilder
) {
    inline fun <reified T : Serializable> NavBackStackEntry.getSerializableArgument(
        key: String
    ): T? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(key, T::class.java)
        } else {
            arguments?.getSerializable(key) as T
        }
    } catch (e: NullPointerException) {
        null
    }

    fun itemListDialog() {
        navGraphBuilder.dialog(NavigationGraphState.CommonView.ItemListDialog.name) { backStackEntry ->
            ItemListDialog(
                navController = navController,
                selectDialog = backStackEntry.getSerializableArgument(NavigationBundleKey.ITEM_LIST)!!
            )
        }
    }

    fun messageDialog() {
        navGraphBuilder.dialog(NavigationGraphState.CommonView.MessageDialog.name) { backStackEntry ->
            MessageDialog(
                navController = navController,
                message = backStackEntry.getSerializableArgument(NavigationBundleKey.MESSAGE)!!
            )
        }
    }

    fun completePaymentPage(
        navGraphBuilder: NavGraphBuilder,
        argument: String = ""
    ) {
        navGraphBuilder.composable("${NavigationGraphState.CommonView.CompletePayment.name}/$argument") {
            CompletePaymentView(
                navController = navController,
                argument = argument,
                complete = it.getSerializableArgument(NavigationBundleKey.RESULT_DATA)!!,
                offlinePaymentViewModel = it.sharedViewModel<OfflinePaymentVM>(navController)
            )
        }
    }

    fun paymentProcessDialog(
        navGraphBuilder: NavGraphBuilder,
        argument: String = ""
    ) {
        navGraphBuilder.dialog("${NavigationGraphState.CreditPaymentView.PaymentProcessDialog.name}/$argument") {
            PaymentProcessDialog(
                navController = navController,
                argument = argument,
                offlinePaymentViewModel = it.sharedViewModel<OfflinePaymentVM>(navController)
            )
        }
    }
}