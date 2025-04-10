package com.example.mtouchpos.view.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.mtouchpos.R
import com.example.mtouchpos.view.navgraph.NavigationBundleKey.Companion.MESSAGE
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.theme.TopNavigation
import com.example.mtouchpos.view.util.SelectDialog
import com.example.mtouchpos.viewmodel.BluetoothCardReaderSettingVM
import com.example.mtouchpos.viewmodel.UsbCardReaderSettingVM

@Composable
fun CardReaderSettingView(
    navController: NavController,
    viewModel: ViewModel
) {
    BackHandler {
        navController.popBackStack(
            route = NavigationGraphState.HomeView.Home.name,
            inclusive = false
        )
    }

    Scaffold(
        topBar = {
            TopNavigation("장치 관리", navController)
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CardReaderSelectTab(
                viewModel = viewModel,
                navController = navController
            )

            when (viewModel) {
                is BluetoothCardReaderSettingVM -> BluetoothList(navController, viewModel)
                is UsbCardReaderSettingVM -> UsbList(navController, viewModel)
            }
        }
    }
}

@Composable
fun CardReaderSelectTab(
    navController: NavController,
    viewModel: ViewModel,
) {
    val watermelonColor = colorResource(R.color.watermelon)

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(top = 20.dp)
            .drawBehind {
                drawLine(
                    watermelonColor,
                    Offset(0f, size.height),
                    Offset(size.width, size.height),
                    1 * density
                )
            }.CardReaderSelectTabModifier(viewModel, watermelonColor),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 10.dp)
                .clickable(onClick = {
                    navController.navigate(NavigationGraphState.DeviceSettingView.Bluetooth.name)
                }),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "블루투스", fontSize = 15.sp)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 10.dp)
                .clickable(onClick = {
                    navController.navigate(NavigationGraphState.DeviceSettingView.USB.name)
                }),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "USB", fontSize = 15.sp)
        }
    }
}

@Composable
fun Modifier.CardReaderSelectTabModifier(
    viewModel: ViewModel,
    watermelonColor: Color
): Modifier = when (viewModel) {
    is BluetoothCardReaderSettingVM -> {
        drawBehind {
            val strokeWidth = 3 * density
            val y = size.height - strokeWidth / 2
            drawLine(
                watermelonColor,
                Offset(0f, y),
                Offset(size.width / 2, y),
                strokeWidth
            )
        }
    }

    is UsbCardReaderSettingVM -> {
        drawBehind {
            val strokeWidth = 3 * density
            val y = size.height - strokeWidth / 2
            drawLine(
                watermelonColor,
                Offset(size.width / 2, y),
                Offset(size.width, y),
                strokeWidth
            )
        }
    }

    else -> this
}

fun navigateErrorDialog(
    navController: NavController,
    message: String
) {
    navController.navigate(
        NavigationGraphState.CommonView.MessageDialog.name,
        bundleOf(MESSAGE to SelectDialog(initValue = message)),
        NavOptions.Builder().setLaunchSingleTop(true).build()
    )
}

@Composable
fun ConnectButton(
    firstLine: String?,
    secondLine: String?,
    isSelected: Boolean,
    onTap: () -> Unit,
    additionButton: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(colorResource(id = if (isSelected) R.color.grey7 else R.color.grey5))
            .clickable(onClick = { onTap() }),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(start = 10.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(0.8f)
            ) {
                Text(
                    text = firstLine ?: "",
                    color = Color.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = secondLine ?: "",
                    color = Color.Black,
                    fontSize = 13.sp
                )
            }

            if(isSelected) {
                Column(
                    modifier = Modifier
                        .width(50.dp)
                        .height(50.dp)
                        .weight(0.2f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(colorResource(id = R.color.coral).copy(0.9f)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "등록됨",
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        }

        if(isSelected) {
            Divider(
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 5.dp),
                color = colorResource(id = R.color.grey8),
                thickness = 3.dp
            )
            additionButton()
        }
    }
}

@Composable
fun AdditionButton(
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(35.dp)
            .padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(colorResource(id = R.color.skyBlue))
            .clickable(onClick = { onClick() }),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color.White
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview6() {
//    DeviceSettingView().connectDeviceView(
//        viewModelFactory = viewModelFactory{ },
//        context = null,
//        owner = null
//    )
}