package com.example.mtouchpos.view.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import com.example.mtouchpos.R
import com.example.mtouchpos.dto.AmountInfo
import com.example.mtouchpos.dto.DirectPaymentInfo
import com.example.mtouchpos.dto.ResponseFlowData
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.theme.MtouchPos
import com.example.mtouchpos.view.util.GradientButton
import com.example.mtouchpos.view.util.ItemListStatus
import com.example.mtouchpos.view.ui.theme.NavigationBundleKey
import com.example.mtouchpos.view.ui.theme.ObserveResultPayment
import com.example.mtouchpos.view.ui.theme.TopNavigation
import com.example.mtouchpos.viewmodel.DirectPaymentViewModel
import com.example.mtouchpos.viewmodel.MainActivityViewModel
import java.lang.IllegalArgumentException
import java.util.Calendar

@Composable
fun DirectPaymentView(
    mainActivityViewModel: MainActivityViewModel = hiltViewModel(),
    directPaymentViewModel: DirectPaymentViewModel = hiltViewModel(),
    navController: NavController = rememberNavController()
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val semiAuth = mainActivityViewModel.getSemiAuth()

    var amount by remember { mutableStateOf(0) }
    var installment by remember { mutableStateOf("") }
    var payerName by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var payerTel by remember { mutableStateOf("") }
    var expirationYear by remember { mutableStateOf("") }
    var expirationMonth by remember { mutableStateOf("") }
    var authPw by remember { mutableStateOf("") }
    var authDob by remember { mutableStateOf("") }

    ObserveResultPayment(
        responseFlowData = directPaymentViewModel.responseFlowData.collectAsStateWithLifecycle(
            ResponseFlowData.Init).value,
        navController = navController,
        responsePage = NavigationGraphState.DirectPaymentView.DirectPayment.name
    )

    Scaffold(
        topBar = {
            TopNavigation("수기 결제")
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
                modifier = Modifier
                    .weight(0.9f)
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                data class TextFieldVO(
                    val key: String,
                    val onTextChange: (String) -> Unit
                )

                mutableListOf(
                    TextFieldVO(key = "구매자명(*)") { payerName = it },
                    TextFieldVO(key = "카드 번호(*)") { cardNumber = it },
                    TextFieldVO(key = "결제 금액(*)") { amount = Integer.parseInt(it) },
                    TextFieldVO(key = "구매자 연락처(*)") { payerTel = it },
                    TextFieldVO(key = "상품명(*)") { productName = it }
                ).mapIndexed { index, textFieldInfo ->
                    DirectPaymentOutlinedTextField(
                        key = textFieldInfo.key,
                        onTextChange = textFieldInfo.onTextChange
                    )
                    if (index == 1) {
                        Row(
                            modifier = Modifier
                                .width((screenWidth * 0.85).dp)
                                .padding(top = 10.dp)
                                .height(65.dp)
                        ) {
                            data class ButtonColumnVO(
                                val itemListStatus: ItemListStatus,
                                val key: String,
                                val value: String
                            )
                            mutableListOf(
                                ButtonColumnVO(
                                    itemListStatus = ItemListStatus(
                                        title = "카드 유효기간(년)",
                                        list = (1..9).map {
                                            (Calendar.getInstance()
                                                .get(Calendar.YEAR) + it).toString()
                                        },
                                        initValue = Calendar.getInstance().get(Calendar.YEAR).toString(),
                                        onTextChange = { expirationYear = it }
                                    ),
                                    key = "유효기간(년)",
                                    value = expirationYear
                                ),
                                ButtonColumnVO(
                                    itemListStatus = ItemListStatus(
                                        title = "카드 유효기간(월)",
                                        list = (1..12).map { String.format("%02d", it) },
                                        initValue = (Calendar.getInstance()
                                            .get(Calendar.MONTH) + 1).toString(),
                                        onTextChange = { expirationMonth = it }
                                    ),
                                    key = "유효기간(월)",
                                    value = expirationMonth
                                ),
                                ButtonColumnVO(
                                    itemListStatus = ItemListStatus(
                                        title = "할부기간",
                                        list = (1..Integer.parseInt(mainActivityViewModel.getMaxInstallment()) + 1).map {
                                            if (it == 1) "일시불" else String.format("%02d", it)
                                        },
                                        initValue = "일시불",
                                        onTextChange = { installment = if(it == "일시불") "00" else it }
                                    ),
                                    key = "할부기간",
                                    value = installment
                                ),
                            ).map {
                                DialogViewTextBox(
                                    key = it.key,
                                    viewValue = it.value,
                                    onclick = {
                                        navController.navigate(
                                            route = NavigationGraphState.CommonView.ItemListDialog.name,
                                            bundle = bundleOf(NavigationBundleKey.ItemList to it.itemListStatus),
                                            navOptions = NavOptions.Builder()
                                                .setLaunchSingleTop(true)
                                                .setPopUpTo(NavigationGraphState.DirectPaymentView.DirectPayment.name,false).build()
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        if (semiAuth == "Y") {
                            Row(
                                modifier = Modifier
                                    .width((screenWidth * 0.85).dp)
                            ) {
                                mutableListOf(
                                    TextFieldVO(key = "비밀번호 앞2자리") { authPw = it },
                                    TextFieldVO(key = "생년월일 6자리") { authDob = it }
                                ).map {
                                    DirectPaymentOutlinedTextField(
                                        key = it.key,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 5.dp),
                                        onTextChange = it.onTextChange
                                    )
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
                    onClick = {
                        try {
                            directPaymentViewModel.requestDirectPayment(
                                amountInfo = AmountInfo(
                                    amount = amount,
                                    installment = installment
                                ),
                                directPaymentInfo = DirectPaymentInfo(
                                    productName = productName,
                                    cardNumber = cardNumber,
                                    expiry = "${expirationYear.substring(2)}${expirationMonth}",
                                    cardAuth = semiAuth,
                                    payerName = payerName,
                                    payerTel = payerTel,
                                    authPw = authPw,
                                    authDob = authDob
                                )
                            )
                        } catch (e: IllegalArgumentException) {
                            navController.navigate(
                                route = NavigationGraphState.CommonView.ErrorDialog.name,
                                bundle = bundleOf(NavigationBundleKey.message to e.message),
                                navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                            )
                        }
                    },
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
    key: String,
    modifier: Modifier = Modifier,
    onTextChange: (String) -> Unit = {}
) {
    var value by remember { mutableStateOf("") }
    OutlinedTextField(
        isError = value == "",
        value = value,
        onValueChange = {
            value = it
            onTextChange(value)
        },
        label = { Text(key) },
        modifier = Modifier
            .width((LocalConfiguration.current.screenWidthDp * 0.85).dp)
            .padding(top = 5.dp)
            .then(modifier)
    )
}

@Preview(showBackground = true)
@Composable
fun DirectPaymentPreView() {
    MtouchPos {
//        DirectPaymentMainView(
//            directPaymentEssentialData = DirectPaymentEssentialData("12", "Y", ""),
//            directPaymentViewModel = null,
//        )
    }
}

@Preview(showBackground = true)
@Composable
fun DirectPaymentErrorDialogPreView() {
    MtouchPos{
//        ErrorDialog(message = "결제 금액을\n입력해주시기 바랍니다")
    }
}

