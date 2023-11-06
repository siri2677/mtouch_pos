package com.example.cleanarchitech_text_0506.view.ui

import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.widget.Toast
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
import androidx.compose.runtime.mutableIntStateOf

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.cleanarchitech_text_0506.R
import com.example.cleanarchitech_text_0506.enum.DeviceType
import com.example.cleanarchitech_text_0506.enum.MainView
import com.example.cleanarchitech_text_0506.enum.PaymentType
import com.example.cleanarchitech_text_0506.enum.SerialCommunicationUsbDialogData
import com.example.cleanarchitech_text_0506.enum.SerialCommunicationMessage
import com.example.cleanarchitech_text_0506.enum.TransactionType
import com.example.cleanarchitech_text_0506.sealed.DeviceConnectSharedFlow
import com.example.cleanarchitech_text_0506.view.ui.theme.CleanArchitech_text_0506Theme
import com.example.cleanarchitech_text_0506.viewmodel.MainActivityViewModel
import com.example.cleanarchitech_text_0506.viewmodel.TestCommunicationViewModel
import com.example.cleanarchitech_text_0506.vo.CompletePaymentViewVO
import com.example.domain.dto.request.tms.RequestPaymentDTO
import com.example.domain.sealed.ResponseTmsAPI
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


enum class CreditPaymentViewClickEvent{
    Empty,
    ViewDialogInstallment,
    ErrorDialog
}

class CreditPaymentView() {
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
            }
            .setNegativeButton("Cancel") { dialog, which ->
            }
            .create()
        alertDialog.show()
    }

//    @Composable
//    fun dialogFormat(vertical: Arrangement.Vertical, horizon: Alignment.Horizontal, backGround: Int, text: String) {
//        Column(
//            modifier = Modifier
//                .width(300.dp)
//                .height(250.dp)
//                .paint(
//                    painterResource(id = backGround),
//                    contentScale = ContentScale.FillBounds
//                ),
//            verticalArrangement = vertical,
//            horizontalAlignment = horizon
//        ) {
//            Text(
//                modifier = Modifier.padding(top = 40.dp),
//                color = colorResource(R.color.white),
//                text = text
//            )
//        }
//    }

//    @Composable
//    fun dialogFormat1(backGround: Int, timer: Int) {
//        Column(
//            modifier = Modifier
//                .width(300.dp)
//                .height(250.dp)
//                .paint(
//                    painterResource(id = backGround),
//                    contentScale = ContentScale.FillBounds
//                ),
//            verticalArrangement = Arrangement.Bottom,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                modifier = Modifier.padding(bottom = 15.dp),
//                color = colorResource(R.color.white),
//                text = if(timer == -1) "결제 대기 시간이 초과 되었습니다" else  "결제 대기 시간: $timer"
//            )
//        }
//    }

    @Composable
    fun dialogFormat(
        vertical: Arrangement.Vertical = Arrangement.Center,
        horizon: Alignment.Horizontal = Alignment.CenterHorizontally,
        backGround: Int,
        text: Unit = Unit
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .height(250.dp)
                .clip(RoundedCornerShape(10.dp))
                .paint(
                    painterResource(id = backGround),
                    contentScale = ContentScale.FillBounds
                ),
            verticalArrangement = vertical,
            horizontalAlignment = horizon
        ) {
            text
        }
    }

    @Composable
    fun usbDevicePaymentDialog(
        navHostController: NavController,
        testCommunicationViewModel: TestCommunicationViewModel,
        deviceConnectSharedFlow: DeviceConnectSharedFlow
    ) {
        Dialog(
            onDismissRequest = {
                testCommunicationViewModel.init()
                navHostController.popBackStack()
            }
        ) {
            var timer by remember { mutableIntStateOf(15) }
            when(deviceConnectSharedFlow) {
                is DeviceConnectSharedFlow.PaymentCompleteFlow -> {
                    testCommunicationViewModel.init()
                    navHostController.popBackStack(
                        route = MainView.CreditPayment.name,
                        inclusive = false
                    )
                }
                is DeviceConnectSharedFlow.SerialCommunicationMessageFlow -> {
                    when(deviceConnectSharedFlow.message) {
                        SerialCommunicationMessage.IcCardInsertRequest.message -> {
                            LaunchedEffect(key1 = timer) {
                                if (timer > -1) {
                                    delay(1000)
                                    timer -= 1
                                }
                                if(timer == -1) {
                                    testCommunicationViewModel.init()
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        navHostController.popBackStack(
                                            route = MainView.CreditPayment.name,
                                            inclusive = false
                                        )
                                    },700)
                                }
                            }
                            dialogFormat(
                                vertical = Arrangement.Bottom,
                                backGround = R.drawable.pb2_4,
                                text = Text(
                                    modifier = Modifier.padding(bottom = 15.dp),
                                    color = colorResource(R.color.white),
                                    text = if(timer == -1) "결제 대기 시간이 초과 되었습니다" else  "결제 대기 시간: $timer"
                                )
                            )
                        }
                        SerialCommunicationMessage.PaymentProgressing.message ->
                            dialogFormat(
                                backGround = R.drawable.pb2_2
                            )
                        SerialCommunicationMessage.FallBackMessage.message ->
                            dialogFormat(
                                backGround = R.drawable.pb2_3
                            )
                        SerialCommunicationMessage.CompletePayment.message -> {
                            dialogFormat(
                                backGround = R.drawable.pb4,
                                text = Text(
                                    modifier = Modifier.padding(top = 40.dp),
                                    color = colorResource(R.color.white),
                                    text = testCommunicationViewModel.getSerialCommunicationMessage()!!
                                )
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }

    @Composable
    fun <T> Flow<T>.CollectAsEffect(
        context: CoroutineContext = EmptyCoroutineContext,
        block: (T) -> Unit
    ) {
        LaunchedEffect(key1 = Unit) {
            onEach(block).flowOn(context).launchIn(this)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun creditPaymentView(
        navHostController: NavController,
        mainActivityViewModel: MainActivityViewModel = hiltViewModel(),
        viewModel: TestCommunicationViewModel = hiltViewModel()
    ) {
        val context = LocalContext.current
        val screenWidth = LocalConfiguration.current.screenWidthDp
        val testCommunicationViewModel = viewModel.setDeviceType()!!
        var installment by remember { mutableStateOf("일시불") }
        var errorMessage by remember { mutableStateOf("") }
        var clickEvent by remember { mutableStateOf(CreditPaymentViewClickEvent.Empty) }
        var sweetAlertDialog by remember {
            mutableStateOf(
                SweetAlertDialog(
                    context,
                    SweetAlertDialog.PROGRESS_TYPE
                )
            )
        }

        serialCommunicationResult(
            testCommunicationViewModel = testCommunicationViewModel,
            navHostController = navHostController,
            dialogMessage = {
                errorMessage = it
                clickEvent = CreditPaymentViewClickEvent.ErrorDialog
            },
            paymentType = PaymentType.Approve
        )

        when (clickEvent) {
            CreditPaymentViewClickEvent.ViewDialogInstallment -> {
                val installmentList = ArrayList<String>()
                for (i in 1 until Integer.parseInt(mainActivityViewModel.getUserInformation().apiMaxInstall) + 1) {
                    installmentList.add(if(i == 1) "일시불" else "${i}개월")
                }
                dialog(
                    title = "할부기간",
                    list = installmentList,
                    initValue = "일시불",
                    onDismissRequest = { clickEvent = CreditPaymentViewClickEvent.Empty },
                    onTextChange = { installment = it }
                )
            }
            CreditPaymentViewClickEvent.ErrorDialog -> {
                errorDialog(
                    message = errorMessage,
                    onDismissRequest = { clickEvent = CreditPaymentViewClickEvent.Empty },
                )
            }
            else -> {}
        }

        DisposableEffect(Unit) {
            onDispose {
                testCommunicationViewModel.serviceUnbind()
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
                        drawLine(
                            Color.LightGray,
                            Offset(0f, 0f),
                            Offset(size.width, 0f),
                            2 * density
                        )
                    },
            ) {
                var account by remember { mutableStateOf("") }
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
                            .clickable {
                                clickEvent = CreditPaymentViewClickEvent.ViewDialogInstallment
                            }
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
                            text = installment
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GradientButton(
                        text = "결제하기",
                        modifier = Modifier
                            .width((screenWidth * 0.95).dp)
                            .padding(vertical = 5.dp)
                            .height(60.dp),
                        fontSize = 20.sp,
                        onClick = {
                            if(testCommunicationViewModel != null) {
                                testCommunicationViewModel.requestOfflinePayment(
                                    RequestPaymentDTO(
                                        amount = account,
                                        installment = installmentFormat(installment),
                                        token = mainActivityViewModel.getUserInformation().key!!
                                    )
                                )
                            } else {
                                errorDialog(context)
                            }
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
                                        account = if (account != null && account.length > 1) {
                                            account.substring(0, account.length - 1)
                                        } else {
                                            "0"
                                        }
                                    } else {
                                        if (account == "0") {
                                            account = item
                                        } else {
                                            account += item
                                        }
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
    private fun installmentFormat(installmentString: String): String {
        return if(installmentString == "일시불") {
            String.format("%02d", 0)
        } else {
            "0${installmentString.replace("0", "").replace("개월", "")}"
        }
    }
}

@Composable
fun serialCommunicationResult(
    testCommunicationViewModel: TestCommunicationViewModel,
    navHostController: NavController,
    dialogMessage: (String) -> Unit = {},
    paymentType: PaymentType
) {
    val context = LocalContext.current
    var sweetAlertDialog by remember {
        mutableStateOf(
            SweetAlertDialog(
                context,
                SweetAlertDialog.PROGRESS_TYPE
            )
        )
    }
    if(testCommunicationViewModel != null) {
        testCommunicationViewModel.deviceConnectSharedFlow.CollectAsEffect(
            block = {
                when(it) {
                    is DeviceConnectSharedFlow.SerialCommunicationMessageFlow -> {
                        when (testCommunicationViewModel.getCurrentRegisteredDeviceType()) {
                            DeviceType.Bluetooth.name -> {
                                if (!sweetAlertDialog.isShowing) {
                                    sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
                                    sweetAlertDialog.progressHelper?.barColor = Color.Green.toArgb()
                                    sweetAlertDialog.setCancelable(true)
                                    sweetAlertDialog.setOnCancelListener(DialogInterface.OnCancelListener {
                                        testCommunicationViewModel.disConnect()
                                        sweetAlertDialog.dismiss()
                                    })
                                }
                                sweetAlertDialog.titleText = it.message
                                sweetAlertDialog.show()
                            }
                            DeviceType.Usb.name -> {
                                val params = bundleOf(
                                    SerialCommunicationUsbDialogData.ViewModel.name to testCommunicationViewModel,
                                    SerialCommunicationUsbDialogData.DeviceConnectSharedFlow.name to it
                                )
                                navHostController.navigate(
                                    MainView.CreditPaymentUsbDialog.name,
                                    params,
                                    NavOptions.Builder().setLaunchSingleTop(true).build()
                                )
                            }
                        }
                    }
                    is DeviceConnectSharedFlow.PaymentCompleteFlow -> {
                        when (testCommunicationViewModel.getCurrentRegisteredDeviceType()) {
                            DeviceType.Bluetooth.name -> {
                                sweetAlertDialog.dismiss()
                            }
                            DeviceType.Usb.name -> {
                                val params = bundleOf(
                                    SerialCommunicationUsbDialogData.ViewModel.name to testCommunicationViewModel,
                                    SerialCommunicationUsbDialogData.DeviceConnectSharedFlow.name to it
                                )
                                navHostController.navigate(
                                    MainView.CreditPaymentUsbDialog.name,
                                    params,
                                    NavOptions.Builder().setLaunchSingleTop(true).build()
                                )
                            }
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            val completePaymentViewVo = CompletePaymentViewVO(
                                transactionType = TransactionType.Offline,
                                paymentType = paymentType,
                                installment = it.responseInsertPaymentDataDTO.installment!!,
                                trackId = it.responseInsertPaymentDataDTO.trackId!!,
                                cardNumber = it.responseInsertPaymentDataDTO.cardNumber!!,
                                amount = it.responseInsertPaymentDataDTO.amount!!,
                                regDay = it.responseInsertPaymentDataDTO.regDay!!,
                                authCode = it.responseInsertPaymentDataDTO.authCode!!,
                                trxId = it.responseInsertPaymentDataDTO.trxId!!
                            )
                            navHostController?.navigate(
                                NavDestination.createRoute(MainView.CompletePayment.name),
                                bundleOf("responsePayAPI" to completePaymentViewVo),
                                NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(MainView.CreditPayment.name, true).build()
                            )
                            Toast.makeText(context, "결제가 완료 되었습니다", Toast.LENGTH_LONG).show()
                        }, 400)
                    }
                    else -> {}
                }
            }
        )

        testCommunicationViewModel.responseTmsAPI.CollectAsEffect(
            block = {
                when(it) {
                    is ResponseTmsAPI.InsertPaymentData -> {
                    }
                    is ResponseTmsAPI.ErrorMessage -> {
                        dialogMessage(it.message)
                    }
                    else -> {}
                }
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CleanArchitech_text_0506Theme {
        CreditPaymentView().dialogFormat(
            backGround = R.drawable.pb4,
            text = Text(
                modifier = Modifier.padding(top = 40.dp),
                color = colorResource(R.color.white),
                text = "결제가 정상적으로 \n 완료 되었습니다."
            )
        )
    }
}