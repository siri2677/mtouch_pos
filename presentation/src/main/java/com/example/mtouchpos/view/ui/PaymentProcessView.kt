package com.example.mtouchpos.view.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.mtouchpos.R
import com.example.mtouchpos.coordinator.OfflinePaymentCoordinator
import com.example.mtouchpos.view.ui.theme.MtouchPos
import com.example.mtouchpos.viewmodel.OfflinePaymentVM
import com.example.mtouchpos.viewmodel.OfflinePaymentVM.Companion.EVENT_FALLBACK
import com.example.mtouchpos.viewmodel.OfflinePaymentVM.Companion.INSERT_IC_CARD
import com.example.mtouchpos.viewmodel.OfflinePaymentVM.Companion.PROCESS_PAYMENT
import com.example.mtouchpos.intent.CardTerminalFactory
import com.example.mtouchpos.view.navgraph.NavigationBundleKey
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.util.LoadingDialogContent
import com.example.mtouchpos.view.util.MessageDialogContent
import com.example.mtouchpos.intent.CardTerminalCommunicateManager
import com.example.mtouchpos.vo.info.PaymentProcessState
import com.example.mtouchpos.vo.type.DeviceType
import kotlinx.coroutines.delay

@Composable
fun PaymentProcessDialog(
    navController: NavController,
    argument: String,
    offlinePaymentViewModel: OfflinePaymentVM
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

    val offlinePaymentInfo = offlinePaymentViewModel.offlinePaymentInfo
        .collectAsStateWithLifecycle().value
    val paymentProcessState = offlinePaymentViewModel.paymentProcessState
        .collectAsStateWithLifecycle().value
    val componentActivity = findComponentActivity(LocalContext.current)
    val offlinePaymentCoordinator = OfflinePaymentCoordinator(
        navController = navController,
        componentActivity = componentActivity,
        offlinePaymentViewModel = offlinePaymentViewModel,
        route = argument
    )
    val cardTerminalCommunicateManager = CardTerminalFactory().getCommunicateManager(
        context = componentActivity,
        launcher = offlinePaymentCoordinator.cardTerminalActivityResult(),
        callBack = offlinePaymentInfo.merchantUrl?.let { CardTerminalFactory.CallBack.External } ?: CardTerminalFactory.CallBack.Home
    )

    LaunchedEffect(paymentProcessState) {
        if(paymentProcessState is PaymentProcessState.Init) {
            permissionCheck(
                context = componentActivity,
                cardTerminalCommunicateManager = cardTerminalCommunicateManager,
                merchantUrl = offlinePaymentInfo.merchantUrl,
                offlinePaymentViewModel = offlinePaymentViewModel,
                offlinePaymentCoordinator = offlinePaymentCoordinator,
            )
        }
    }

    Dialog(
        onDismissRequest = {
            offlinePaymentViewModel.stopRetry()
            offlinePaymentInfo.merchantUrl?.let {
                offlinePaymentCoordinator.configureIntent(
                    isSuccess = false,
                    message = "고객취소",
                    merchantUrl = it
                )
            } ?: navController.popBackStack()
        }
    ) {
        when(paymentProcessState) {
            PaymentProcessState.Loading, is PaymentProcessState.Approve -> LoadingDialogContent()

            is PaymentProcessState.Error -> {
                offlinePaymentInfo.merchantUrl?.let {
                    offlinePaymentCoordinator.configureIntent(
                        isSuccess = false,
                        message = paymentProcessState.message,
                        merchantUrl = it
                    )
                } ?: if (cardTerminalCommunicateManager == null) {
                    MessageDialogContent(
                        navController = navController,
                        message = paymentProcessState.message
                    )
                } else {
                    offlinePaymentCoordinator.navigateToErrorDialog(paymentProcessState.message)
                }
            }

            is PaymentProcessState.Complete -> {
                offlinePaymentInfo.merchantUrl?.let {
                    offlinePaymentCoordinator.configureIntent(
                        isSuccess = true,
                        message = "성공",
                        merchantUrl = it,
                        paymentProcessState = paymentProcessState
                    )
                } ?: run {
                    navController.navigate(
                        route = "${NavigationGraphState.CommonView.CompletePayment.name}/${argument}",
                        bundle = bundleOf(
                            NavigationBundleKey.RESULT_DATA to paymentProcessState
                        ),
                        navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(
                            NavigationGraphState.HomeView.Home.name, false).build()
                    )
                }
            }

            is PaymentProcessState.CommunicateCardReader -> {
                if(paymentProcessState is PaymentProcessState.CommunicateCardReader.Error) {
                    offlinePaymentInfo.merchantUrl?.let {
                        offlinePaymentCoordinator.configureIntent(
                            isSuccess = false,
                            message = paymentProcessState.message,
                            merchantUrl = it
                        )
                    } ?: MessageDialogContent(
                        navController = navController,
                        message = paymentProcessState.message
                    )
                } else {
                    BluetoothCommunicateContent(paymentProcessState)
                }
            }

            else -> {}
        }
    }
}

fun permissionCheck(
    context: ComponentActivity,
    cardTerminalCommunicateManager: CardTerminalCommunicateManager?,
    merchantUrl: String?,
    offlinePaymentViewModel: OfflinePaymentVM,
    offlinePaymentCoordinator: OfflinePaymentCoordinator
) {
    val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    var devices = usbManager.deviceList.values.toList()

    when(val cardReader = offlinePaymentViewModel.getCurrentCardReaderData()) {
        is DeviceType.Bluetooth -> if(getBluetoothPermissionsArray().all { context.checkPermission(it, 0, 0) == PackageManager.PERMISSION_GRANTED }) {
            offlinePaymentViewModel.requestOfflinePayment(cardTerminalCommunicateManager)
        } else {
            merchantUrl?.let {
                offlinePaymentCoordinator.configureIntent(
                    isSuccess = false,
                    message = "장치 권한 미허용",
                    merchantUrl = it
                )
            } ?: offlinePaymentCoordinator.navigateToErrorDialog("장치 권한 미허용")
        }

        is DeviceType.Usb -> devices.firstOrNull { infoFormat(it.toString()) == infoFormat(cardReader.deviceInformation) }?.let {
            if(usbManager.hasPermission(it)) {
                offlinePaymentViewModel.requestOfflinePayment(cardTerminalCommunicateManager)
            } else {
                merchantUrl?.let {
                    offlinePaymentCoordinator.configureIntent(
                        isSuccess = false,
                        message = "장치 권한 미허용",
                        merchantUrl = it
                    )
                } ?: offlinePaymentCoordinator.navigateToErrorDialog("장치 권한 미허용")
            }
        }

        null -> {
            cardTerminalCommunicateManager?.let {
                offlinePaymentViewModel.requestOfflinePayment(it)
            } ?: merchantUrl?.let {
                offlinePaymentCoordinator.configureIntent(
                    isSuccess = false,
                    message = "등록된 장치 없음",
                    merchantUrl = it
                )
            } ?: offlinePaymentCoordinator.navigateToErrorDialog("등록된 장치 없음")
        }
    }
}

@Composable
fun BluetoothCommunicateContent(paymentProcessState: PaymentProcessState.CommunicateCardReader) {
    when(paymentProcessState) {
        is PaymentProcessState.CommunicateCardReader.Fallback -> {
            DeviceCommunicateContainer(
                modifier = Modifier.background(colorResource(id = R.color.white)),
                visibleProgressbar = true,
                text = { Text(text = EVENT_FALLBACK) }
            )
        }

        PaymentProcessState.CommunicateCardReader.InsertIC -> {
            DeviceCommunicateContainer(
                modifier = Modifier.background(colorResource(id = R.color.white)),
                visibleProgressbar = true,
                text = { Text(text = INSERT_IC_CARD) }
            )
        }

        PaymentProcessState.CommunicateCardReader.ReadingIC -> {
            DeviceCommunicateContainer(
                modifier = Modifier.background(colorResource(id = R.color.white)),
                visibleProgressbar = true,
                text = { Text(text = PROCESS_PAYMENT) }
            )
        }

        is PaymentProcessState.CommunicateCardReader.Connecting -> {
            DeviceCommunicateContainer(
                modifier = Modifier.background(colorResource(id = R.color.white)),
                visibleProgressbar = true,
                text = {
                    Text(
                        text = if (paymentProcessState.retryCount == 0) {
                            "리더기 연결 진행중입니다."
                        } else {
                            "리더기 연결 재시도 중입니다 ${paymentProcessState.retryCount}.."
                        }
                    )
                }
            )
        }

        else -> {}
    }
}

@Composable
fun UsbCommunicateContent(
    navController: NavController,
    offlinePaymentViewModel: OfflinePaymentVM,
    paymentProcessState: PaymentProcessState.CommunicateCardReader
) {
    when(paymentProcessState) {
        is PaymentProcessState.CommunicateCardReader.Fallback -> {
            DeviceCommunicateContainer(
                modifier = Modifier.paint(
                    painter = painterResource(id = R.drawable.pb2_3),
                    contentScale = ContentScale.FillBounds
                )
            )
        }

        PaymentProcessState.CommunicateCardReader.InsertIC -> {
            var timer by remember { mutableIntStateOf(15) }

            LaunchedEffect(timer) {
                if (timer > -1) {
                    delay(1000)
                    timer -= 1
                } else {
                    delay(700)
                    navController.popBackStack()
                    offlinePaymentViewModel.stopRetry()
                }
            }

            DeviceCommunicateContainer(
                vertical = Arrangement.Bottom,
                modifier = Modifier.paint(
                    painter = painterResource(id = R.drawable.pb2_4),
                    contentScale = ContentScale.FillBounds
                ),
                text = {
                    Text(
                        modifier = Modifier.padding(bottom = 15.dp),
                        color = colorResource(R.color.white),
                        text = if (timer == -1) "결제 대기 시간이 초과 되었습니다" else "결제 대기 시간: $timer"
                    )
                }
            )
        }

        PaymentProcessState.CommunicateCardReader.ReadingIC -> {
            DeviceCommunicateContainer(
                modifier = Modifier.paint(
                    painter = painterResource(id = R.drawable.pb2_2),
                    contentScale = ContentScale.FillBounds
                )
            )
        }

        is PaymentProcessState.CommunicateCardReader.Connecting -> {
            DeviceCommunicateContainer(
                vertical = Arrangement.Bottom,
                modifier = Modifier.paint(
                    painter = painterResource(id = R.drawable.pb4),
                    contentScale = ContentScale.FillBounds
                ),
                visibleProgressbar = true,
                text = {
                    Text(
                        modifier = Modifier.padding(bottom = 15.dp),
                        color = colorResource(R.color.white),
                        text = if (paymentProcessState.retryCount == 0) {
                            "USB 리더기 연결 진행중입니다."
                        } else {
                            "리더기 연결 재시도 중입니다 ${paymentProcessState.retryCount}.."
                        }
                    )
                }
            )
        }

        else -> {}
    }
}

@Composable
fun DeviceCommunicateContainer(
    vertical: Arrangement.Vertical = Arrangement.Center,
    horizon: Alignment.Horizontal = Alignment.CenterHorizontally,
    modifier: Modifier,
    visibleProgressbar: Boolean = false,
    text: @Composable () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .width(300.dp)
            .height(250.dp)
            .clip(RoundedCornerShape(10.dp))
            .then(modifier),
        verticalArrangement = vertical,
        horizontalAlignment = horizon
    ) {
        if(visibleProgressbar) CircularProgressIndicator(modifier = Modifier.padding(bottom = 40.dp))
        text()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MtouchPos {
        DeviceCommunicateContainer(
            modifier = Modifier.paint(
                painter = painterResource(id = R.drawable.pb4),
                contentScale = ContentScale.FillBounds
            ),
            text = {
                Text(
                    modifier = Modifier.padding(top = 40.dp),
                    color = colorResource(R.color.white),
                    text = "결제가 정상적으로 \n 완료 되었습니다."
                )
            }
        )
    }
}