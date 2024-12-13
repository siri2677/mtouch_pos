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
import com.example.mtouchpos.view.util.ErrorDialog
import com.example.mtouchpos.view.util.ItemListDialog
import com.example.mtouchpos.viewmodel.OfflinePaymentVM
import java.io.Serializable

object CompleteDestination {
    const val ROUTE = "complete"
    const val ARGUMENT_KEY = "param" // Optional argument
}

object PaymentProcessDestination {
    const val ROUTE = "paymentProcess"
    const val ARGUMENT_KEY = "param" // Optional argument
}

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

    fun errorDialog() {
        navGraphBuilder.dialog(NavigationGraphState.CommonView.ErrorDialog.name) { backStackEntry ->
            ErrorDialog(
                navController = navController,
                message = backStackEntry.getSerializableArgument(NavigationBundleKey.MESSAGE)!!,
//                reDirectPage = backStackEntry.getSerializableArgument(NavigationBundleKey.reDirectPage)!!
//                onDismissRequest = backStackEntry.getSerializableArgument(NavigationBundleKey.onDismiss)
            )
        }
    }

    fun completePaymentPage(
        navGraphBuilder: NavGraphBuilder,
        argument: String = ""
    ) {
        navGraphBuilder.composable("${NavigationGraphState.CommonView.CompletePayment.name}$argument") { backStackEntry ->
            Log.w("argument", argument)
            CompletePaymentView(
                navController = navController,
                argument = argument,
                completePaymentViewInfo = backStackEntry.getSerializableArgument(
                    NavigationBundleKey.RESULT_DATA
                )!!,
                offlinePaymentViewModel = backStackEntry.sharedViewModel<OfflinePaymentVM>(navController),
                beforeNavGraph = backStackEntry.getSerializableArgument(
                    NavigationBundleKey.BEFORE_NAVGRAPH
                )!!
            )
        }
    }

    fun paymentProcessDialog(
        navGraphBuilder: NavGraphBuilder,
        argument: String = ""
    ) {
        navGraphBuilder.dialog("${NavigationGraphState.CreditPaymentView.PaymentProcessDialog.name}$argument") { backStackEntry ->
            Log.w("argument", argument)
            PaymentProcessDialog(
                navController = navController,
                argument = argument,
                offlinePaymentViewModel = backStackEntry.sharedViewModel<OfflinePaymentVM>(navController)
            )
        }
    }
}