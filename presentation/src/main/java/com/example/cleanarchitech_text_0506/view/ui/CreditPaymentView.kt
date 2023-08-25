package com.example.cleanarchitech_text_0506.view.ui

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.createRoute
import androidx.navigation.NavOptions
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.cleanarchitech_text_0506.R
import com.example.cleanarchitech_text_0506.enum.SerialCommunicationUsbDialogData
import com.example.cleanarchitech_text_0506.enum.NavigationView
import com.example.cleanarchitech_text_0506.view.ui.theme.CleanArchitech_text_0506Theme
import com.example.cleanarchitech_text_0506.viewmodel.DeviceSerialCommunicateViewModel
import com.example.domain.enumclass.DeviceType
import com.example.domain.enumclass.SerialCommunicationMessage
import com.mtouch.ksr02_03_04_v2.Domain.Model.EncMSRManager
import kotlinx.coroutines.delay

class CreditPaymentView {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun topNavigationConnectDevice() {
        CenterAlignedTopAppBar(
            title = {
                Text("신용 결제", fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
                Icon(Icons.Default.ArrowBack, contentDescription = "Menu")
            }
        )
    }

    private fun errorDialog(context: Context) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Title")
            .setMessage("기기를 등록하거나 연결 후 결제 바랍니다")
            .setPositiveButton("OK") { dialog, which ->
                // OK 버튼을 눌렀을 때 처리할 로직을 작성합니다.
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // Cancel 버튼을 눌렀을 때 처리할 로직을 작성합니다.
            }
            .create()
        alertDialog.show()
    }

    @Composable
    fun dialogFormat(vertical: Arrangement.Vertical, horizon: Alignment.Horizontal, backGround: Int, text: String) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .height(250.dp)
                .paint(
                    painterResource(id = backGround),
                    contentScale = ContentScale.FillBounds
                ),
            verticalArrangement = vertical,
            horizontalAlignment = horizon
        ) {
            Text(
                modifier = Modifier.padding(top = 40.dp),
                color = colorResource(R.color.white),
                text = text
            )
        }
    }

    @Composable
    fun dialogFormat1(backGround: Int, timer: Int) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .height(250.dp)
                .paint(
                    painterResource(id = backGround),
                    contentScale = ContentScale.FillBounds
                ),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(bottom = 15.dp),
                color = colorResource(R.color.white),
                text = if(timer == -1) "결제 대기 시간이 초과 되었습니다" else  "결제 대기 시간: $timer"
            )
        }
    }

    @Composable
    fun dialogFormat(backGround: Int) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .height(250.dp)
                .clip(RoundedCornerShape(10.dp))
                .paint(
                    painterResource(id = backGround),
                    contentScale = ContentScale.FillBounds
                )
        ) {
        }
    }

    @Composable
    fun usbDevicePaymentDialog(navHostController: NavController, viewModel: DeviceSerialCommunicateViewModel) {
        Dialog(
            onDismissRequest = {
                viewModel.init()
                navHostController.popBackStack()
            }
        ) {
            var timer by remember { mutableStateOf(15) }
            when (viewModel.dialogMessageProperty) {
                SerialCommunicationMessage.IcCardInsertRequest.message -> {
                    LaunchedEffect(key1 = timer) {
                        if (timer > -1) {
                            delay(1000)
                            timer -= 1
                        }
                    }
                    dialogFormat1(R.drawable.pb2_4, timer)
                    if(timer == -1) {
                        viewModel.init()
                        Handler(Looper.getMainLooper()).postDelayed({
                            navHostController.popBackStack(
                                route = NavigationView.CreditPayment.name,
                                inclusive = false
                            )
                        },700)
                    }
                }
                SerialCommunicationMessage.PaymentProgressing.message ->
                    dialogFormat(R.drawable.pb2_2)
                SerialCommunicationMessage.FallBackMessage.message ->
                    dialogFormat(R.drawable.pb2_3)
                SerialCommunicationMessage.CompletePayment.message ->
                    dialogFormat(Arrangement.Center, Alignment.CenterHorizontally, R.drawable.pb4, viewModel.dialogMessageProperty)
                SerialCommunicationUsbDialogData.SerialCommunicationFinish.name -> {
                    viewModel.init()
                    navHostController.popBackStack(
                        route = NavigationView.CreditPayment.name,
                        inclusive = false
                    )
                }
            }
        }
    }

    private fun NavController.navigate(route: String, bundle: Bundle = Bundle(), navOptions: NavOptions) {
        val r = createRoute(route)
        navigate(r.hashCode(), bundle, navOptions)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun creditPaymentView(
        navHostController: NavController,
        context: Context?,
        owner: LifecycleOwner?,
        deviceSerialCommunicateVieModel: DeviceSerialCommunicateViewModel = hiltViewModel()
    ) {
        val screenWidth = LocalConfiguration.current.screenWidthDp
        var sweetAlertDialog by remember {
            mutableStateOf(
                SweetAlertDialog(
                    context,
                    SweetAlertDialog.PROGRESS_TYPE
                )
            )
        }

        var account by remember { mutableStateOf("") }
        DisposableEffect(Unit) {
//            deviceSerialCommunicateVieModel.errorOccur?.observe(owner!!) {
//                it?.getContentIfNotHandled()?.let { data ->
//                    if (data) errorDialog(context!!)
//                }
//            }
            deviceSerialCommunicateVieModel.serialCommunicateMessage?.observe(owner!!) {
                it?.getContentIfNotHandled()?.let { data ->
                    when(deviceSerialCommunicateVieModel.getCurrentRegisteredDeviceType()) {
                        DeviceType.BLUETOOTH -> {
                            if(!sweetAlertDialog.isShowing) {
                                sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
                                sweetAlertDialog.progressHelper?.barColor = Color.Green.toArgb()
                                sweetAlertDialog.setCancelable(true)
                                sweetAlertDialog.setOnCancelListener(DialogInterface.OnCancelListener {
                                    deviceSerialCommunicateVieModel.init()
                                    sweetAlertDialog.dismiss()
                                })
                            }
                            sweetAlertDialog.titleText = data
                            sweetAlertDialog.show()
                        }
                        DeviceType.USB -> {
                            deviceSerialCommunicateVieModel.dialogMessageProperty = data
                            deviceSerialCommunicateVieModel.ownerProperty = owner
                            val params = bundleOf(
                                SerialCommunicationUsbDialogData.ViewModel.name to deviceSerialCommunicateVieModel
                            )
                            navHostController.navigate(
                                NavigationView.CreditPaymentUsbDialog.name,
                                params,
                                NavOptions.Builder().setLaunchSingleTop(true).build()
                            )
                        }
                    }
                }
            }
            deviceSerialCommunicateVieModel.notExistRegisteredDevice?.observe(owner!!) {
                it?.getContentIfNotHandled()?.let { data ->
                    sweetAlertDialog.titleText = data
                }
            }
            deviceSerialCommunicateVieModel.isCompletePayment?.observe(owner!!) {
                it?.getContentIfNotHandled()?.let {
                    when(deviceSerialCommunicateVieModel.getCurrentRegisteredDeviceType()) {
                        DeviceType.BLUETOOTH -> {
                            sweetAlertDialog.dismiss()
                        }
                        DeviceType.USB -> {
                            deviceSerialCommunicateVieModel.dialogMessageProperty = SerialCommunicationUsbDialogData.SerialCommunicationFinish.name
                            deviceSerialCommunicateVieModel.ownerProperty = owner
                            val params = bundleOf(
                                SerialCommunicationUsbDialogData.ViewModel.name to deviceSerialCommunicateVieModel
                            )
                            navHostController.navigate(
                                NavigationView.CreditPaymentUsbDialog.name,
                                params,
                                NavOptions.Builder().setLaunchSingleTop(true).build()
                            )
                        }
                    }
                }
            }

            onDispose {
                deviceSerialCommunicateVieModel.unbindService()
            }
        }

        Scaffold(
            topBar = {
                topNavigationConnectDevice()
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .drawBehind {
                        val strokeWidth = 2 * density
                        drawLine(
                            Color.LightGray,
                            Offset(0f, 0f),
                            Offset(size.width, 0f),
                            strokeWidth
                        )
                    },
            ) {
                Column(
                    modifier = Modifier.padding(start = 5.dp, end = 5.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(top = 15.dp, start = 5.dp, end = 5.dp),
                        text = "결제 금액을 입력해주세요."
                    )
                    Column(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(colorResource(id = R.color.white))
                            .border(
                                width = 1.dp,
                                color = colorResource(id = R.color.grey4),
                                shape = RectangleShape
                            ),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End
                    ) {
                        Row() {
                            Text(
                                modifier = Modifier.padding(end = 5.dp),
                                text = account
                            )
                            Text(
                                modifier = Modifier.padding(end = 5.dp),
                                text = "원"
                            )
                        }

                    }

                    Text(
                        modifier = Modifier.padding(top = 8.dp, start = 5.dp, end = 5.dp),
                        text = "할부기간을 선택해주세요."
                    )
                    Column(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(colorResource(id = R.color.white))
                            .border(
                                width = 1.dp,
                                color = colorResource(id = R.color.grey4),
                                shape = RectangleShape
                            ),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier.padding(end = 5.dp),
                            text = "일시불"
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoginView().GradientButton(
                        text = "결제하기",
                        modifier = Modifier
                            .width((screenWidth * 0.95).dp)
                            .padding(vertical = 5.dp)
                            .height(60.dp),
                        fontSize = 20.sp,
                        onClick = {
                            deviceSerialCommunicateVieModel!!.requestDeviceSerialCommunication(
                                EncMSRManager().makeDongleInfo()
                            )
                        }
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(15.dp),
                    modifier = Modifier.fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val numberPad = ArrayList<String>(
                        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "00", "0", "delete")
                    )
                    itemsIndexed(numberPad) { index, item ->
                        Column(
                            modifier = Modifier
                                .paint(
                                    painterResource(id = R.drawable.card_bt),
                                    contentScale = ContentScale.FillBounds
                                )
                                .height(60.dp)
                                .background(color = colorResource(id = R.color.grey4))
                                .clickable(onClick = {
                                    if (item == "delete") {
                                        if (account != null && account.length > 1) {
                                            account = account.substring(0, account.length - 1)
                                        } else {
                                            "0"
                                        }
                                    } else {
                                        if (account == "0") {

                                        }
                                        account += item
                                    }
                                }),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (item == "delete") {
                                Image(
                                    painter = painterResource(R.drawable.ic_back),
                                    contentDescription = null
                                )
                            } else {
                                Text(
                                    text = item,
                                    fontSize = 35.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CleanArchitech_text_0506Theme {
        CreditPaymentView().dialogFormat(
            Arrangement.Center,
            Alignment.CenterHorizontally,
            R.drawable.pb4,
            "결제가 정상적으로 \n 완료 되었습니다.")
    }
}