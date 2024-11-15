package com.example.mtouchpos.view.navgraph

import android.os.Build
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.example.mtouchpos.view.ui.CompletePaymentPage
import com.example.mtouchpos.view.ui.PaymentProcessDialog
import com.example.mtouchpos.view.util.ErrorDialog
import com.example.mtouchpos.view.util.ItemListDialog
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

    fun completePaymentPage() {
        with(navGraphBuilder) {
            composable(NavigationGraphState.CommonView.CompletePayment.name) { backStackEntry ->
                CompletePaymentPage(
                    navController = navController,
                    completePaymentViewInfo = backStackEntry.getSerializableArgument(
                        NavigationBundleKey.RESULT_DATA
                    )!!,
                    beforeNavGraph = backStackEntry.getSerializableArgument(
                        NavigationBundleKey.BEFORE_NAVGRAPH
                    )!!
                )
            }

            dialog(NavigationGraphState.CreditPaymentView.PaymentProcessDialog.name) { backStackEntry ->
                PaymentProcessDialog(
                    navController = navController,
                    paymentProcess = backStackEntry.getSerializableArgument(
                        NavigationBundleKey.DIALOG_DATA
                    )!!
                )
            }
        }
    }
}