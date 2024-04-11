package com.example.mtouchpos.view.ui

import android.os.Handler
import android.os.Looper
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.mtouchpos.R
import com.example.mtouchpos.view.ui.theme.MtouchPos
import com.example.mtouchpos.vo.DeviceSerialCommunicate
import kotlinx.coroutines.delay

@Composable
fun BluetoothDevicePaymentDialog(
    navController: NavController,
    deviceSerialCommunicate: DeviceSerialCommunicate
) {
    Dialog(
        onDismissRequest = { navController.popBackStack() }
    ) {
        val modifier = Modifier.background(colorResource(id = R.color.white))
        when (deviceSerialCommunicate) {
            is DeviceSerialCommunicate.SerialCommunicationMessage.IcCardInsertRequest -> {
                DialogFormat(
                    modifier = modifier,
                    visibleProgressbar = true,
                    text = { Text(text = deviceSerialCommunicate.message) }
                )
            }
            is DeviceSerialCommunicate.SerialCommunicationMessage.PaymentProgressing -> {
                DialogFormat(
                    modifier = modifier,
                    visibleProgressbar = true,
                    text = { Text(text = deviceSerialCommunicate.message) }
                )
            }
            is DeviceSerialCommunicate.SerialCommunicationMessage.FallBackMessage -> {
                DialogFormat(
                    modifier = modifier,
                    visibleProgressbar = true,
                    text = { Text(text = deviceSerialCommunicate.message) }
                )
            }
//                is DeviceSerialCommunicate.PaymentCompleteFlow -> deviceSerialCommunicate.completeProcess
            else -> {}
        }
    }
}

@Composable
fun UsbDevicePaymentDialog(
    navController: NavController,
    deviceSerialCommunicate: DeviceSerialCommunicate
) {
    Dialog(
        onDismissRequest = { navController.popBackStack() }
    ) {
        when (deviceSerialCommunicate) {
            is DeviceSerialCommunicate.SerialCommunicationMessage.IcCardInsertRequest -> {
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
                DialogFormat(
                    vertical = Arrangement.Bottom,
                    modifier = Modifier.paint(
                        painter = painterResource(id = deviceSerialCommunicate.painterInt),
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
            is DeviceSerialCommunicate.SerialCommunicationMessage.PaymentProgressing -> {
                DialogFormat(
                    modifier = Modifier.paint(
                        painter = painterResource(id = deviceSerialCommunicate.painterInt),
                        contentScale = ContentScale.FillBounds
                    )
                )
            }
            is DeviceSerialCommunicate.SerialCommunicationMessage.FallBackMessage -> {
                DialogFormat(
                    modifier = Modifier.paint(
                        painter = painterResource(id = deviceSerialCommunicate.painterInt),
                        contentScale = ContentScale.FillBounds
                    )
                )
            }
//            is DeviceSerialCommunicate.PaymentCompleteFlow -> deviceSerialCommunicate.completeProcess
            else -> {}
        }
    }
}

@Composable
fun DialogFormat(
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
        DialogFormat(
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