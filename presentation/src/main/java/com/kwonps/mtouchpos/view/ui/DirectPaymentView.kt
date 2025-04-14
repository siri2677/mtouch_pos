package com.kwonps.mtouchpos.view.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import com.kwonps.mtouchpos.R
import com.kwonps.mtouchpos.coordinator.DirectPaymentCoordinator
import com.kwonps.mtouchpos.view.navgraph.NavigationBundleKey
import com.kwonps.mtouchpos.view.navgraph.NavigationGraphState
import com.kwonps.mtouchpos.view.ui.theme.TopNavigation
import com.kwonps.mtouchpos.view.util.GradientButton
import com.kwonps.mtouchpos.view.util.SelectDialog
import com.kwonps.mtouchpos.viewmodel.DirectPaymentVM
import com.kwonps.mtouchpos.viewmodel.LoginVM
import java.util.Calendar

@Composable
fun DirectPaymentView(
    mainActivityViewModel: LoginVM = hiltViewModel(),
    directPaymentViewModel: DirectPaymentVM = hiltViewModel(),
    navController: NavController = rememberNavController()
) {
    data class TextBox(
        val label: String,
        val text: String,
        val onTextChange: (String) -> DirectPaymentVM.DirectPaymentInfo
    )

    data class ButtonColumn(
        val key: String,
        val value: String,
        val list: List<String>,
        val initValue: String,
        val onTextChange: (String) -> DirectPaymentVM.DirectPaymentInfo
    )

    val currentConnectedUserInfo = mainActivityViewModel.fetchCurrentConnectedUserInfo()
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val directPaymentViewInfo = directPaymentViewModel.directPaymentInfo.collectAsStateWithLifecycle().value

    DirectPaymentCoordinator(navController).observeResultPaymentData(
        directPaymentViewModel.reactDirectPaymentInfo
            .collectAsStateWithLifecycle().value
    )

    Scaffold(
        topBar = { TopNavigation("수기 결제", navController) }
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
                modifier = Modifier
                    .weight(0.9f)
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                with(directPaymentViewInfo) {
                    listOf(
                        TextBox("구매자명(*)", payerName) { copy(payerName = it) },
                        TextBox("카드 번호(*)", cardNumber) { copy(cardNumber = it) },
                        TextBox("결제 금액(*)", amount) { copy(amount = it)  },
                        TextBox("구매자 연락처(*)", payerTel) { copy(payerTel = it) },
                        TextBox("상품명(*)", productName) { copy(productName = it)  }
                    ).forEachIndexed { index, textFieldInfo ->
                        DirectPaymentOutlinedTextField(
                            label = textFieldInfo.label,
                            text = textFieldInfo.text,
                            onTextChange = { directPaymentViewModel.updateDirectPaymentInfo(textFieldInfo.onTextChange(it)) }
                        )

                        if (index == 1) {
                            Row(
                                modifier = Modifier
                                    .width((screenWidth * 0.85).dp)
                                    .padding(top = 10.dp)
                                    .height(65.dp)
                            ) {
                                listOf(
                                    ButtonColumn(
                                        key = "카드 유효기간(년)",
                                        value = expirationYear,
                                        list = (1..9).map { (Calendar.getInstance().get(Calendar.YEAR) + it).toString() },
                                        initValue = Calendar.getInstance().get(Calendar.YEAR).toString(),
                                        onTextChange = { copy(expirationYear = it) }
                                    ),
                                    ButtonColumn(
                                        key = "카드 유효기간(월)",
                                        value = expirationMonth,
                                        list = (1..12).map { String.format("%02d", it) },
                                        initValue = (Calendar.getInstance().get(Calendar.MONTH) + 1).toString(),
                                        onTextChange = { copy(expirationMonth = it) }
                                    ),
                                    ButtonColumn(
                                        key = "할부기간",
                                        value = installment,
                                        list = (1..Integer.parseInt(currentConnectedUserInfo?.apiMaxInstall) + 1).map {
                                            if (it == 1) "일시불" else String.format("%02d", it)
                                        },
                                        initValue = "일시불",
                                        onTextChange = { copy(installment = it) }
                                    )
                                ).forEach { buttonColumn ->
                                    DialogViewTextBox(
                                        key = buttonColumn.key,
                                        viewValue = buttonColumn.value,
                                        onclick = {
                                            navController.navigate(
                                                route = NavigationGraphState.CommonView.ItemListDialog.name,
                                                bundle =
                                                    bundleOf(
                                                        NavigationBundleKey.ITEM_LIST to SelectDialog(
                                                            title = buttonColumn.key,
                                                            list = buttonColumn.list,
                                                            initValue = buttonColumn.initValue,
                                                            onTextChange = { directPaymentViewModel.updateDirectPaymentInfo(buttonColumn.onTextChange(it)) }
                                                        )
                                                    ),
                                                navOptions = NavOptions.Builder()
                                                    .setLaunchSingleTop(true)
                                                    .setPopUpTo(NavigationGraphState.DirectPaymentView.DirectPayment.name,false).build()
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }

                            if (currentConnectedUserInfo?.semiAuth == "Y") {
                                Row(
                                    modifier = Modifier.width((screenWidth * 0.85).dp)
                                ) {
                                    listOf(
                                        TextBox("비밀번호 앞2자리", directPaymentViewInfo.authPw ?: "") { copy(authPw = it) },
                                        TextBox("생년월일 6자리", directPaymentViewInfo.authDob ?: "") { copy(authDob = it) }
                                    ).forEach { textBox ->
                                        DirectPaymentOutlinedTextField(
                                            text = textBox.text,
                                            label = textBox.label,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 5.dp),
                                            onTextChange = { directPaymentViewModel.updateDirectPaymentInfo(textBox.onTextChange(it)) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(0.12f)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GradientButton(
                    text = "결제 하기",
                    modifier = Modifier
                        .width((screenWidth * 0.85).dp)
                        .height(50.dp),
                    onClick = { directPaymentViewModel.requestDirectPayment() },
                    fontSize = 16.sp,
                    roundedCornerShapeSize = 0
                )
            }
        }
    }
}

@Composable
fun DialogViewTextBox(
    onclick: () -> Unit,
    viewValue: String,
    modifier: Modifier,
    key: String
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.grey4),
                shape = RectangleShape
            )
            .clickable { onclick() }
            .then(modifier),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = key,
                fontSize = 14.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Text(
                modifier = Modifier
                    .weight(0.8f),
                text = viewValue,
                textAlign = TextAlign.Center
            )
            Image(
                modifier = Modifier
                    .padding(top = 8.dp, end = 10.dp)
                    .weight(0.2f),
                painter = painterResource(id = R.drawable.down_arrow),
                contentDescription = "down_arrow",
                contentScale = ContentScale.FillBounds,
            )
        }

    }
}

@Composable
fun DirectPaymentOutlinedTextField(
    label: String,
    text: String,
    modifier: Modifier = Modifier,
    onTextChange: (String) -> Unit = {}
) {
    OutlinedTextField(
        isError = text == "",
        value = text,
        onValueChange = { onTextChange(it) },
        label = { Text(label) },
        modifier = Modifier
            .width((LocalConfiguration.current.screenWidthDp * 0.85).dp)
            .padding(top = 5.dp)
            .then(modifier)
    )
}
