package com.example.mtouchpos.view.ui

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.theme.MtouchPos
import com.example.mtouchpos.view.navgraph.NavigationBundleKey
import com.example.mtouchpos.view.ui.theme.TopNavigation
import com.example.mtouchpos.view.ui.theme.observeCompletePaymentInfo
import com.example.mtouchpos.view.util.GradientButton
import com.example.mtouchpos.view.util.SelectDialog
import com.example.mtouchpos.viewmodel.DirectPaymentViewModel
import com.example.mtouchpos.viewmodel.LoginViewModel
import com.example.mtouchpos.vo.type.UseCaseResult
import java.util.Calendar

@Composable
fun test(): ManagedActivityResultLauncher<Intent, ActivityResult> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 결과 처리 로직, 필요한 경우
        // 예: result.resultCode를 체크하여 결과를 처리
    }
}

@Composable
fun DirectPaymentView(
    mainActivityViewModel: LoginViewModel = hiltViewModel(),
    directPaymentViewModel: DirectPaymentViewModel = hiltViewModel(),
    navController: NavController = rememberNavController()
) {
    data class TextBox(
        val label: String,
        val text: String,
        val onTextChange: (String) -> DirectPaymentViewModel.DirectPaymentInfo
    )

    data class ButtonColumn(
        val key: String,
        val value: String,
        val list: List<String>,
        val initValue: String,
        val onTextChange: (String) -> DirectPaymentViewModel.DirectPaymentInfo
    )

    val currentConnectedUserInfo = mainActivityViewModel.fetchCurrentConnectedUserInfo()
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val directPaymentViewInfo = directPaymentViewModel.directPaymentInfo.collectAsStateWithLifecycle().value

    directPaymentViewModel.reactDirectPaymentInfo
        .collectAsStateWithLifecycle(UseCaseResult.Init).value
        .observeCompletePaymentInfo(navController)

    Scaffold(
        topBar = { TopNavigation("수기 결제") }
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
                with(directPaymentViewModel.directPaymentInfo) {
                    listOf(
                        TextBox("구매자명(*)", directPaymentViewInfo.payerName) { value.copy(payerName = it) },
                        TextBox("카드 번호(*)", directPaymentViewInfo.cardNumber) { value.copy(cardNumber = it) },
                        TextBox("결제 금액(*)", directPaymentViewInfo.amount) { value.copy(amount = it)  },
                        TextBox("구매자 연락처(*)", directPaymentViewInfo.payerTel) {value.copy(payerTel = it) },
                        TextBox("상품명(*)", directPaymentViewInfo.productName) { value.copy(productName = it)  }
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
                                        value = directPaymentViewInfo.expirationYear,
                                        list = (1..9).map { (Calendar.getInstance().get(Calendar.YEAR) + it).toString() },
                                        initValue = Calendar.getInstance().get(Calendar.YEAR).toString(),
                                        onTextChange = { value.copy(expirationYear = it) }
                                    ),
                                    ButtonColumn(
                                        key = "카드 유효기간(월)",
                                        value = directPaymentViewInfo.expirationMonth,
                                        list = (1..12).map { String.format("%02d", it) },
                                        initValue = (Calendar.getInstance().get(Calendar.MONTH) + 1).toString(),
                                        onTextChange = { value.copy(expirationMonth = it) }
                                    ),
                                    ButtonColumn(
                                        key = "할부기간",
                                        value = directPaymentViewInfo.installment,
                                        list = (1..Integer.parseInt(currentConnectedUserInfo?.apiMaxInstall) + 1).map {
                                            if (it == 1) "일시불" else String.format("%02d", it)
                                        },
                                        initValue = "일시불",
                                        onTextChange = { value.copy(installment = it) }
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
                                        TextBox("비밀번호 앞2자리", directPaymentViewInfo.authPw ?: "") { value.copy(authPw = it) },
                                        TextBox("생년월일 6자리", directPaymentViewInfo.authDob ?: "") { value.copy(authDob = it) }
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
        onValueChange = { onTextChange(text) },
        label = { Text(label) },
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

