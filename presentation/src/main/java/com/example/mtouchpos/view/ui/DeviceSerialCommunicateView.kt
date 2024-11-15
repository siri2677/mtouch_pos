package com.example.mtouchpos.view.ui

import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.mtouchpos.R
import com.example.mtouchpos.coordinator.OfflinePaymentCoordinator
import com.example.mtouchpos.view.ui.theme.MtouchPos
import com.example.mtouchpos.view.util.ErrorDialogContent
import com.example.mtouchpos.viewmodel.DeviceSettingViewModel
import com.example.mtouchpos.viewmodel.OfflinePaymentViewModel
import com.example.mtouchpos.viewmodel.OfflinePaymentViewModel.Companion.EVENT_FALLBACK
import com.example.mtouchpos.viewmodel.OfflinePaymentViewModel.Companion.INSERT_IC_CARD
import com.example.mtouchpos.viewmodel.OfflinePaymentViewModel.Companion.PROCESS_PAYMENT
import com.example.mtouchpos.viewmodel.factory.CardTerminalFactory
import kotlinx.coroutines.delay

@Composable
fun PaymentProcessDialog(
    navController: NavController,
    paymentProcess: OfflinePaymentCoordinator.PaymentProcess
) {
    fun findComponentActivity(context: Context): ComponentActivity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is ComponentActivity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }

    val offlinePaymentViewModel = hiltViewModel<OfflinePaymentViewModel>()
    val paymentProcessState = offlinePaymentViewModel.paymentProcessState
        .collectAsStateWithLifecycle(OfflinePaymentViewModel.PaymentProcessState.Loading).value
    val componentActivity = findComponentActivity(LocalContext.current)
    val offlinePaymentCoordinator = OfflinePaymentCoordinator(navController, componentActivity!!, offlinePaymentViewModel)
    val communicateCardTerminalManager = CardTerminalFactory(
        context = componentActivity,
        launcher = offlinePaymentCoordinator.cardTerminalActivityResult(paymentProcess.merchantUrl),
        callBack = paymentProcess.merchantUrl?.let { CardTerminalFactory.CallBack.External } ?: CardTerminalFactory.CallBack.Home
    )

    LaunchedEffect(Unit) {
        if(offlinePaymentViewModel.vanTrxId != null) return@LaunchedEffect
        when(paymentProcess.offlinePaymentInfo) {
            is OfflinePaymentViewModel.OfflinePaymentInfo.Approve -> {
                offlinePaymentViewModel.updateOfflinePaymentInfo(paymentProcess.offlinePaymentInfo)
                offlinePaymentViewModel.requestOfflinePayment(communicateCardTerminalManager)
            }
            is OfflinePaymentViewModel.OfflinePaymentInfo.Cancel -> {
                offlinePaymentViewModel.updateOfflineCancelPaymentInfo(paymentProcess.offlinePaymentInfo)
                offlinePaymentViewModel.requestOfflineCancelPayment(communicateCardTerminalManager)
            }
        }
    }

    Dialog(
        onDismissRequest = {
            paymentProcess.merchantUrl?.let {
                offlinePaymentCoordinator.configureIntent(
                    isSuccess = false,
                    message = "고객취소",
                    merchantUrl = it
                )
            } ?: navController.popBackStack()
        }
    ) {
        offlinePaymentCoordinator.paymentResult(
            paymentProcessState = paymentProcessState,
            communicateCardTerminalManager = communicateCardTerminalManager,
            merchantUrl = paymentProcess.merchantUrl
        ) {
            when(offlinePaymentViewModel.fetchConnectedDeviceInfo()) {
                is DeviceSettingViewModel.BluetoothDeviceInfo -> {
                    BluetoothPaymentDialogContent(paymentProcessState)
                }

                is DeviceSettingViewModel.UsbDeviceInfo -> {
                    UsbPaymentDialogContent(
                        navController = navController,
                        paymentProcessState = paymentProcessState
                    )
                }

                else -> {
                    ErrorDialogContent(
                        navController = navController,
                        message = "장치 등록 후 결제 진행 바시기 바랍니다."
                    )
                }
            }
        }
    }
}

@Composable
fun BluetoothPaymentDialogContent(paymentProcessState: OfflinePaymentViewModel.PaymentProcessState) {
    when(paymentProcessState) {
        is OfflinePaymentViewModel.PaymentProcessState.Fallback -> {
            PaymentDialogLayout(
                modifier = Modifier.background(colorResource(id = R.color.white)),
                visibleProgressbar = true,
                text = { Text(text = EVENT_FALLBACK) }
            )
        }

        OfflinePaymentViewModel.PaymentProcessState.InsertIC -> {
            PaymentDialogLayout(
                modifier = Modifier.background(colorResource(id = R.color.white)),
                visibleProgressbar = true,
                text = { Text(text = INSERT_IC_CARD) }
            )
        }

        OfflinePaymentViewModel.PaymentProcessState.ReadingIC -> {
            PaymentDialogLayout(
                modifier = Modifier.background(colorResource(id = R.color.white)),
                visibleProgressbar = true,
                text = { Text(text = PROCESS_PAYMENT) }
            )
        }

        else -> {}
    }
}

@Composable
fun UsbPaymentDialogContent(
    navController: NavController,
    paymentProcessState: OfflinePaymentViewModel.PaymentProcessState
) {
    when(paymentProcessState) {
        is OfflinePaymentViewModel.PaymentProcessState.Fallback -> {
            PaymentDialogLayout(
                modifier = Modifier.paint(
                    painter = painterResource(id = R.drawable.pb2_3),
                    contentScale = ContentScale.FillBounds
                )
            )
        }

        OfflinePaymentViewModel.PaymentProcessState.InsertIC -> {
            var timer by remember { mutableIntStateOf(15) }

            LaunchedEffect(timer) {
                if (timer > -1) {
                    delay(1000)
                    timer -= 1
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        navController.popBackStack()
                    }, 700)
                }
            }

            PaymentDialogLayout(
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

        OfflinePaymentViewModel.PaymentProcessState.ReadingIC -> {
            PaymentDialogLayout(
                modifier = Modifier.paint(
                    painter = painterResource(id = R.drawable.pb2_2),
                    contentScale = ContentScale.FillBounds
                )
            )
        }

        else -> {}
    }
}

@Composable
fun PaymentDialogLayout(
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
        if(visibleProgressbar) CircularProgressIndicator()
        text()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MtouchPos {
        PaymentDialogLayout(
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