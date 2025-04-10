package com.example.mtouchpos.view.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.navigation.NavController
import com.example.mtouchpos.view.util.MessageDialogContent
import com.google.gson.Gson


@Composable
fun PermissionLauncher(
    navController: NavController,
    permissionArray: Array<String>
): ActivityResultLauncher<Array<String>> = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    if (permissionArray.all { permissions[it] == true }) {
        navController.setPermissionResult(true)
    } else {
        navController.setPermissionResult(false)
    }
    navController.popBackStack()
}

@Composable
fun PermissionSettingViewLauncher(
    navController: NavController,
    context: Context,
    permissionArray: Array<String>
): ManagedActivityResultLauncher<Intent, ActivityResult> = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    if(permissionArray.all { context.checkSelfPermission(it) == PERMISSION_GRANTED }) {
        navController.setPermissionResult(true)
    } else {
        navController.setPermissionResult(false)
        navController.popBackStack()
    }
}

@Composable
fun ManifestPermissionRequestView(
    navController: NavController,
    permissionJson: String
) {
    fun findComponentActivity(context: Context): ComponentActivity {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is ComponentActivity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        throw IllegalStateException("ComponentActivity를 찾을 수 없습니다.")
    }

    val context = LocalContext.current
    val permissionArray = Gson().fromJson(permissionJson, Array<String>::class.java)
    val permissionLauncher = PermissionLauncher(navController, permissionArray)
    val permissionSettingViewLauncher = PermissionSettingViewLauncher(navController, context, permissionArray)

    var isDialogClosed by remember { mutableStateOf(false) }
    var isPermissionGranted = remember {
        mutableStateOf(permissionArray.all { context.checkSelfPermission(it) == PERMISSION_GRANTED })
    }
    var isTwiceDenied = remember {
        mutableStateOf(permissionArray.all { shouldShowRequestPermissionRationale(findComponentActivity(context), it) })
    }

    if(isPermissionGranted.value) {
        navController.setPermissionResult(true)
        navController.popBackStack()
    }

    if(!isDialogClosed) {
        Dialog(
            onDismissRequest = {
                navController.setPermissionResult(false)
                navController.popBackStack()
            }
        ) {
            if(isTwiceDenied.value) {
                MessageDialogContent(
                    message = "결제 단말기 연동에 필요한\n앱 권한이 거부된 상태입니다.\n확인버튼 클릭시 권한 요청이 진행됩니다",
                    checkButtonProcess = {
                        isDialogClosed = true
                        permissionLauncher.launch(permissionArray)
                    }
                )
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                MessageDialogContent(
                    message = "결제 단말기 연동에 필요한\n앱 권한이 거부된 상태입니다.\n확인버튼 클릭시 앱 권한 페이지로 이동합니다.",
                    checkButtonProcess = {
                        isDialogClosed = true
                        permissionSettingViewLauncher.launch(intent)
                    }
                )
            }
        }
    }
}

fun NavController.setPermissionResult(value: Boolean) {
    previousBackStackEntry?.savedStateHandle?.set("result", value)
}
