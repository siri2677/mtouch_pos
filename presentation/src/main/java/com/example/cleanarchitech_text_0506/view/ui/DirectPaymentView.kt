package com.example.cleanarchitech_text_0506.view.ui

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import com.example.cleanarchitech_text_0506.R
import com.example.cleanarchitech_text_0506.enum.MainView
import com.example.cleanarchitech_text_0506.enum.PaymentType
import com.example.cleanarchitech_text_0506.enum.TransactionType
import com.example.cleanarchitech_text_0506.view.ui.theme.CleanArchitech_text_0506Theme
import com.example.cleanarchitech_text_0506.viewmodel.DirectPaymentViewModel
import com.example.cleanarchitech_text_0506.viewmodel.MainActivityViewModel
import com.example.cleanarchitech_text_0506.vo.CompletePaymentViewVO
import com.example.domain.dto.request.pay.RequestDirectPaymentCard
import com.example.domain.dto.request.pay.RequestDirectPaymentDto
import com.example.domain.dto.request.pay.RequestDirectPaymentMetadata
import com.example.domain.dto.request.pay.RequestDirectPaymentProduct
import com.example.domain.sealed.ResponsePayAPI
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar

enum class DirectPaymentViewClickEvent{
    Empty,
    ViewDialogExpirationYear,
    ViewDialogExpirationMonth,
    ViewDialogInstallment,
    CheckValidation
}

data class DirectPaymentEssentialData(
    val apiMaxInstall: String,
    val semiAuth: String,
    val payKey: String
)

@Composable
fun DirectPaymentView(
    mainActivityViewModel: MainActivityViewModel = hiltViewModel(),
    directPaymentViewModel: DirectPaymentViewModel = hiltViewModel(),
    navHostController: NavController,
){
    DirectPaymentMainView(
        DirectPaymentEssentialData(
            mainActivityViewModel.getUserInformation().apiMaxInstall!!,
            mainActivityViewModel.getUserInformation().semiAuth!!,
            mainActivityViewModel.getUserInformation().payKey!!,
        ),
        directPaymentViewModel,
        navHostController
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectPaymentMainView(
    directPaymentEssentialData: DirectPaymentEssentialData,
    directPaymentViewModel: DirectPaymentViewModel?,
    navHostController: NavController = rememberNavController()
) {
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp
    var clickEvent by remember { mutableStateOf(DirectPaymentViewClickEvent.Empty) }

    directPaymentViewModel?.responseDirectPayment?.CollectAsEffect(
        block = {
            when (it) {
                is ResponsePayAPI.DirectPaymentContent -> {
                    val completePaymentViewVo = CompletePaymentViewVO(
                        TransactionType.Direct,
                        PaymentType.Approve,
                        it.responseDirectPaymentDto.pay?.card?.installment.toString(),
                        it.responseDirectPaymentDto.pay?.trackId!!,
                        it.responseDirectPaymentDto.pay?.card?.bin!!,
                        it.responseDirectPaymentDto.pay?.amount.toString(),
                        it.responseDirectPaymentDto.result.create,
                        it.responseDirectPaymentDto.pay?.authCd!!,
                        it.responseDirectPaymentDto.pay?.trxId!!
                    )
                    navHostController?.navigate(
                        MainView.CompletePayment.name,
                        bundleOf("responsePayAPI" to completePaymentViewVo),
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                    Toast.makeText(context, "결제가 완료 되었습니다", Toast.LENGTH_LONG).show()
                }

                is ResponsePayAPI.ErrorMessage -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        }
    )

    Scaffold(
        topBar = {
            topNavigation("수기 결제")
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
                var expirationYear by remember {
                    mutableStateOf(
                        String.format(
                            "%02d",
                            Calendar.getInstance().get(Calendar.YEAR)
                        )
                    )
                }
                var expirationMonth by remember {
                    mutableStateOf(
                        String.format(
                            "%02d",
                            (Calendar.getInstance().get(Calendar.MONTH) + 1)
                        )
                    )
                }
                var installment by remember { mutableStateOf("일시불") }
                var password by remember { mutableStateOf("") }
                var birthday by remember { mutableStateOf("") }
                var payerName by remember { mutableStateOf("") }
                var cardNumber by remember { mutableStateOf("") }
                var amount by remember { mutableStateOf("") }
                var payerTel by remember { mutableStateOf("") }
                var productName by remember { mutableStateOf("") }

                textField(key = "구매자명(*)", onTextChange = { payerName = it })
                textField(key = "카드 번호(*)", onTextChange = { cardNumber = it })

                Row(
                    modifier = Modifier
                        .width((screenWidth * 0.85).dp)
                        .padding(top = 10.dp)
                        .height(65.dp)
                ) {
                    buttonColumn(
                        onclick = {
                            clickEvent = DirectPaymentViewClickEvent.ViewDialogExpirationYear
                        },
                        viewValue = expirationYear,
                        modifier = Modifier.weight(1f),
                        key = "유효기간(년)"
                    )
                    buttonColumn(
                        onclick = {
                            clickEvent = DirectPaymentViewClickEvent.ViewDialogExpirationMonth
                        },
                        viewValue = expirationMonth,
                        modifier = Modifier.weight(1f),
                        key = "유효기간(월)"
                    )
                    buttonColumn(
                        onclick = {
                            clickEvent = DirectPaymentViewClickEvent.ViewDialogInstallment
                        },
                        viewValue = installment,
                        modifier = Modifier.weight(1f),
                        key = "할부기간"
                    )
                }

                if (directPaymentEssentialData.semiAuth == "Y") {
                    Row(
                        modifier = Modifier
                            .width((screenWidth * 0.85).dp)
                    ) {
                        textField(
                            key = "비밀번호 앞2자리",
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 5.dp),
                            onTextChange = { password = it }
                        )
                        textField(
                            key = "생년월일 6자리",
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 5.dp),
                            onTextChange = { birthday = it }
                        )
                    }
                }

                textField(key = "결제 금액(*)", onTextChange = { amount = it })
                textField(key = "구매자 연락처(*)", onTextChange = { payerTel = it })
                textField(key = "상품명(*)", onTextChange = { productName = it })

                when (clickEvent) {
                    DirectPaymentViewClickEvent.ViewDialogExpirationYear -> {
                        val yearList = ArrayList<String>()
                        for (i in 1..9) {
                            yearList.add((Calendar.getInstance().get(Calendar.YEAR) + i).toString())
                        }
                        dialog(
                            title = "카드 유효기간(년)",
                            list = yearList,
                            initValue = Calendar.getInstance().get(Calendar.YEAR).toString(),
                            onDismissRequest = { clickEvent = DirectPaymentViewClickEvent.Empty },
                            onTextChange = { expirationYear = it }
                        )
                    }

                    DirectPaymentViewClickEvent.ViewDialogExpirationMonth -> {
                        val monthList = ArrayList<String>()
                        for (i in 1..12) {
                            monthList.add(String.format("%02d", i))
                        }
                        dialog(
                            title = "카드 유효기간(월)",
                            list = monthList,
                            initValue = (Calendar.getInstance().get(Calendar.MONTH) + 1).toString(),
                            onDismissRequest = { clickEvent = DirectPaymentViewClickEvent.Empty },
                            onTextChange = { expirationMonth = it }
                        )
                    }

                    DirectPaymentViewClickEvent.ViewDialogInstallment -> {
                        val installmentList = ArrayList<String>()
                        for (i in 1 until Integer.parseInt(directPaymentEssentialData.apiMaxInstall) + 1) {
                            installmentList.add(if (i == 1) "일시불" else String.format("%02d", i))
                        }
                        dialog(
                            title = "할부기간",
                            list = installmentList,
                            initValue = "일시불",
                            onDismissRequest = { clickEvent = DirectPaymentViewClickEvent.Empty },
                            onTextChange = { installment = it }
                        )
                    }

                    DirectPaymentViewClickEvent.CheckValidation -> {
                        val requestDirectPaymentDto = RequestDirectPaymentDto(
                            payKey = directPaymentEssentialData.payKey,
                            amount = amount,
                            payerName = payerName,
                            payerTel = payerTel,
                            card = RequestDirectPaymentCard(
                                number = cardNumber,
                                expiry = expirationYear.substring(2) + expirationMonth,
                                installment = if (installment == "일시불") "00" else installment
                            ),
                            product = RequestDirectPaymentProduct(
                                name = productName,
                                qty = 1,
                                price = amount
                            ),
                            metadata =
                            if (directPaymentEssentialData.semiAuth == "Y") {
                                RequestDirectPaymentMetadata("true", password, birthday)
                            } else {
                                RequestDirectPaymentMetadata("false")
                            }
                        )
                        val errorMessage: String? = validation(requestDirectPaymentDto)
                        if (errorMessage != null) {
                            errorDialog(
                                message = errorMessage,
                                onDismissRequest = {
                                    clickEvent = DirectPaymentViewClickEvent.Empty
                                },
                            )
                        } else {
                            clickEvent = DirectPaymentViewClickEvent.Empty
                            directPaymentViewModel!!.requestDirectPayment(requestDirectPaymentDto)
                        }
                    }
                    else -> {}
                }
            }

            Column(
                modifier = Modifier
                    .weight(0.12f)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                GradientButton(
                    text = "결제 하기",
                    modifier = Modifier
                        .width((screenWidth * 0.85).dp)
                        .height(50.dp),
                    onClick = { clickEvent = DirectPaymentViewClickEvent.CheckValidation },
                    fontSize = 16.sp,
                    roundedCornerShapeSize = 0
                )
            }
        }
    }
}

fun validation(requestDirectPaymentDto: RequestDirectPaymentDto): String? {
    if (requestDirectPaymentDto.payerName == "") return "구매자명을\n 입력해주시기 바랍니다"
    if (requestDirectPaymentDto.card.number == "") return "카드 번호를\n 입력해주시기 바랍니다"
    if (requestDirectPaymentDto.amount == "") return "결제 금액을\n 입력해주시기 바랍니다"
    if (requestDirectPaymentDto.payerTel == "") return "구매자 연락처를\n 입력해주시기 바랍니다"
    if (requestDirectPaymentDto.product.name == "") return "상품명을\n 입력해주시기 바랍니다"
    if (requestDirectPaymentDto.metadata?.cardAuth == "true"){
        if(requestDirectPaymentDto.metadata?.authPw == "") return "비밀번호 앞 2자리를\n 입력해주시기 바랍니다"
        if(requestDirectPaymentDto.metadata?.authDob == "") return "생년월일을\n 입력해주시기 바랍니다"
    }
    return null
}


@Composable
fun buttonColumn(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun textField(key: String, modifier: Modifier = Modifier, onTextChange: (String) -> Unit = {}): String {
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
    return value
}

@Composable
fun dialog(
    title: String,
    list: ArrayList<String>,
    initValue: String = "",
    onDismissRequest: () -> Unit,
    onTextChange: (String) -> Unit
) {
    var dialogVisibility by remember { mutableStateOf(true) }
    var selectedValue by remember { mutableStateOf(initValue) }
    if(dialogVisibility) {
        Dialog(
            onDismissRequest = {
                onDismissRequest()
                dialogVisibility = false
            }
        ) {
            Column(
                modifier = Modifier
                    .width((LocalConfiguration.current.screenWidthDp * 0.6).dp)
                    .wrapContentHeight()
                    .background(colorResource(id = R.color.white)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(top = 10.dp),
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Divider(
                    modifier = Modifier.padding(top = 10.dp),
                    color = Color.LightGray,
                    thickness = 0.8.dp
                )

                LazyColumn(
                    modifier = Modifier
                        .height(300.dp)
                ) {
                    item {
                        list.forEachIndexed { index, gridItem ->
                            Row(
                                modifier = Modifier
                                    .padding(top = 3.dp, start = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colorResource(id = R.color.watermelon)
                                    ),
                                    selected = selectedValue == gridItem,
                                    onClick = { selectedValue = gridItem }
                                )
                                Text(
                                    text = gridItem,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedValue = gridItem }
                                )
                            }
                            Divider(
                                color = colorResource(id = R.color.grey7),
                                thickness = 0.3.dp
                            )
                        }
                    }
                }
                GradientButton(
                    text = "확인",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    onClick = {
                        onTextChange(selectedValue)
                        onDismissRequest()
                        dialogVisibility = false
                    },
                    fontSize = 16.sp,
                    roundedCornerShapeSize = 0
                )
            }
        }
    }
}

@Composable
fun errorDialog(
    message: String,
    onDismissRequest: () -> Unit = {},
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    var dialogVisibility by remember { mutableStateOf(true) }
    if (dialogVisibility) {
        Dialog(
            onDismissRequest = {
                onDismissRequest()
                dialogVisibility = false
            }
        ) {
            Column(
                modifier = Modifier
                    .width((LocalConfiguration.current.screenWidthDp * 0.7).dp)
                    .height(220.dp)
                    .background(colorResource(id = R.color.white)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier
                        .padding(top = 10.dp),
                    text = "알림"
                )
                Divider(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .width((screenWidth * 0.5).dp),
                    color = Color.LightGray,
                    thickness = 0.8.dp
                )
                Text(
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 10.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = message,
                )
                GradientButton(
                    text = "확인",
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width((screenWidth * 0.5).dp)
                        .height(50.dp),
                    onClick = {
                        onDismissRequest()
                        dialogVisibility = false
                    },
                    fontSize = 16.sp,
                    roundedCornerShapeSize = 0
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun topNavigation(title: String) {
    CenterAlignedTopAppBar(
        title = {
            Text(title, fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
            Icon(Icons.Default.ArrowBack, contentDescription = "Menu")
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DirectPaymentPreView() {
    CleanArchitech_text_0506Theme {
        DirectPaymentMainView(
            directPaymentEssentialData = DirectPaymentEssentialData("12", "Y", ""),
            directPaymentViewModel = null,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DirectPaymentErrorDialogPreView() {
    CleanArchitech_text_0506Theme{
        errorDialog(message = "결제 금액을\n입력해주시기 바랍니다") {}
    }
}

