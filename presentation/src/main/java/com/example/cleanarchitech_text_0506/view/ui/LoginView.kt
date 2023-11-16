package com.example.cleanarchitech_text_0506.view.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.isPopupLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import com.example.cleanarchitech_text_0506.R
import com.example.cleanarchitech_text_0506.enum.MainView
import com.example.cleanarchitech_text_0506.viewmodel.MainActivityViewModel
import com.example.domain.dto.request.tms.RequestGetUserInformationDto
import com.example.domain.dto.response.tms.ResponseGetUserInformationDto

data class MainGridItem(val imageRes: Int, val text: String, val mainView: MainView)
class LoginView() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun pgIdLoginDialog(
        navHostController: NavHostController,
        mainActivityViewModel: MainActivityViewModel = hiltViewModel()
    ) {
        val context = LocalContext.current
        val screenWidth = LocalConfiguration.current.screenWidthDp
        var merchantId by remember { mutableStateOf("") }
        var terminalId by remember { mutableStateOf("") }
        var serialNumber by remember { mutableStateOf("") }

        val keyTmsResponseBodyCollectAsState by mainActivityViewModel?.userInformation!!.collectAsStateWithLifecycle(
            initialValue = ResponseGetUserInformationDto(
                tmnId = "",
                bankName = "",
                phoneNo = "",
                result = "",
                Authorization = "",
                semiAuth = "",
                identity = "",
                accntHolder = "",
                appDirect = "",
                ceoName = "",
                addr = "",
                key = "",
                agencyEmail = "",
                distEmail = "",
                vat = "",
                agencyTel = "",
                agencyName = "",
                telNo = "",
                apiMaxInstall = "",
                distTel = "",
                name = "",
                distName = "",
                payKey = "",
                account = ""
            )
        )
        if(keyTmsResponseBodyCollectAsState.tmnId != "") {
            Toast.makeText(context, "로그인이 완료되었습니다", Toast.LENGTH_SHORT).show()
            navHostController.navigate(
                route = MainView.Main.name,
                navOptions = NavOptions.Builder().setRestoreState(true).build()
            )
        }

        Dialog(
            onDismissRequest = { navHostController.popBackStack() }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("PGID 로그인", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    OutlinedTextField(
                        value = merchantId,
                        onValueChange = { merchantId = it },
                        label = { Text("가맹점 ID") },
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(top = 10.dp)
                    )
                    OutlinedTextField(
                        value = terminalId,
                        onValueChange = { terminalId = it },
                        label = { Text("터미널 ID") },
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(top = 5.dp)
                    )
                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        label = { Text("시리얼 번호") },
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(top = 5.dp)
                    )
                    GradientButton(
                        text = "로그인",
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(top = 12.dp),
                        onClick = {
                            mainActivityViewModel.login(
                                RequestGetUserInformationDto(
                                    mchtId = merchantId,
                                    tmnId = terminalId,
                                    serial = serialNumber,
                                    appId = null,
                                    version = null,
                                    telNo = null
                                )
                            )
                        },
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun vanIdLoginDialog(navHostController: NavHostController) {
        val screenWidth = LocalConfiguration.current.screenWidthDp
        var text by remember { mutableStateOf("") }
        Dialog(
            onDismissRequest = { navHostController.popBackStack() }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("VANID 로그인", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("단말기 번호") },
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(top = 10.dp)
                    )
                    GradientButton(
                        text = "로그인",
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(vertical = 12.dp),
                        fontSize = 16.sp
                    )

                }
            }
        }
    }

    @Composable
    fun registeredIdDialog(
        navHostController: NavHostController,
        mainViewModel: MainActivityViewModel = hiltViewModel()
    ) {
        var selectedIndex by remember { mutableStateOf(-2) }
        val screenWidth = LocalConfiguration.current.screenWidthDp
        var tapItem by remember { mutableStateOf(
                RequestGetUserInformationDto(
                    tmnId = "",
                    serial = "",
                    mchtId = "",
                    appId = null,
                    version = null,
                    telNo = null,
                )
            )
        }
        var userInformationList by remember { mutableStateOf(mainViewModel.getUserInformationRepository()) }

        val keyTmsResponseBodyCollectAsState by mainViewModel.userInformation.collectAsStateWithLifecycle(
            initialValue = ResponseGetUserInformationDto(
                tmnId = "",
                bankName = "",
                phoneNo = "",
                result = "",
                Authorization = "",
                semiAuth = "",
                identity = "",
                accntHolder = "",
                appDirect = "",
                ceoName = "",
                addr = "",
                key = "",
                agencyEmail = "",
                distEmail = "",
                vat = "",
                agencyTel = "",
                agencyName = "",
                telNo = "",
                apiMaxInstall = "",
                distTel = "",
                name = "",
                distName = "",
                payKey = "",
                account = ""
            )
        )
        if(keyTmsResponseBodyCollectAsState.tmnId != "") {
            Toast.makeText(LocalContext.current, "로그인이 완료되었습니다", Toast.LENGTH_SHORT).show()
            navHostController.navigate(
                route = MainView.Main.name,
                navOptions = NavOptions.Builder().setRestoreState(true).build()
            )
        }
        Dialog(
            onDismissRequest = { navHostController.popBackStack() }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("등록된 아이디 조회", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    LazyColumn(
                        modifier = Modifier
                            .height(300.dp)
                            .padding(top = 10.dp),
                    ) {
                        item {
                            userInformationList.forEachIndexed { index, gridItem ->
                                registeredIdList(
                                    index = index,
                                    tmnId = gridItem.tmnId!!,
                                    isSelected = selectedIndex == index,
                                    onItemTap = {
                                        selectedIndex = index
                                        tapItem = gridItem
                                    },
                                    onDeleteTap = {
                                        mainViewModel.deleteUserInformationRepository(gridItem.tmnId!!)
                                        userInformationList = mainViewModel.getUserInformationRepository()
                                    }
                                )

                            }
                        }
                    }
                    GradientButton(
                        text = "로그인",
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(vertical = 12.dp),
                        fontSize = 16.sp,
                        onClick = {
                            mainViewModel.login(
                                RequestGetUserInformationDto(
                                    mchtId = tapItem.mchtId,
                                    tmnId = tapItem.tmnId,
                                    serial = tapItem.serial,
                                    appId = null,
                                    version = null,
                                    telNo = null
                                )
                            )
                        }
                    )
                }
            }
        }
    }


    @Composable
    fun registeredIdList(
        isSelected: Boolean,
        onItemTap: () -> Unit,
        onDeleteTap: () -> Unit,
        index: Int,
        tmnId: String
    ) {
        val screenWidth = LocalConfiguration.current.screenWidthDp
        val color = if (isSelected) colorResource(id = R.color.grey5) else Color.White
        Button(
            modifier = Modifier.width((screenWidth * 0.7).dp),
            onClick = { onItemTap() },
            border = BorderStroke(1.dp, Color.Black),
            shape = RoundedCornerShape(10),
            colors = ButtonDefaults.buttonColors(containerColor = color)
        ) {
            Row() {
                Text(
                    text = tmnId,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.8f)
                )
                Text(
                    text = "삭제",
                    modifier = Modifier
                        .weight(0.2f)
                        .clickable(
                            onClick = { onDeleteTap() }
                        ),
                    color = Color.Black,
                    textAlign = TextAlign.End,
                )
            }
        }
    }

    @Composable
    fun loginDialog(
        navHostController: NavHostController,
    ) {
        val screenWidth = LocalConfiguration.current.screenWidthDp
        Dialog(
            onDismissRequest = { navHostController.popBackStack() }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GradientButton(
                        text = "PGID 로그인",
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(vertical = 10.dp),
                        onClick = { navHostController.navigate(MainView.PgIdLogin.name) },
                        fontSize = 16.sp
                    )
                    GradientButton(
                        text = "VANID 로그인",
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(vertical = 10.dp),
                        onClick = { navHostController.navigate(MainView.VanIdLogin.name) },
                        fontSize = 16.sp
                    )
                    GradientButton(
                        text = "등록된 아이디 조회",
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(vertical = 10.dp),
                        onClick = { navHostController.navigate(MainView.RegisteredId.name) },
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
    fontSize: TextUnit,
    roundedCornerShapeSize: Int = 6
) {
    Button(
        modifier = modifier
            .clip(RoundedCornerShape(roundedCornerShapeSize.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        colorResource(id = R.color.orange_pink),
                        colorResource(id = R.color.watermelon)
                    )
                )
            ),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        onClick = { onClick() },
    ) {
        Text(
            color = colorResource(id = R.color.white),
            text = text,
            fontSize = fontSize
        )
    }
}

//fun GradientButtonColumn() {
//    Column(
//        modifier = modifier,
//        shape = RoundedCornerShape(roundedCornerShapeSize.dp),
//        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//        contentPadding = PaddingValues(),
//        onClick = { onClick() },
//    ) {
//        Box(
//            modifier = Modifier
//                .background(
//                    Brush.horizontalGradient(
//                        colors = listOf(
//                            colorResource(id = R.color.orange_pink),
//                            colorResource(id = R.color.watermelon)
//                        )
//                    )
//                ).then(modifier),
//            contentAlignment = Alignment.Center,
//
//            ) {
//            Text(
//                text = text,
//                fontSize = fontSize
//            )
//        }
//    }
//}

@Preview(showBackground = true)
@Composable
fun loginDiaglog() {
    val context = LocalContext.current
    val navController = rememberNavController()
    LoginView().loginDialog(navController)
}
@Preview(showBackground = true)
@Composable
fun pgloginDiaglog() {
    val navController = rememberNavController()
//    LoginView().pgIdLoginDialog(navController)
}
@Preview(showBackground = true)
@Composable
fun vanloginDiaglog() {
    val navController = rememberNavController()
    LoginView().vanIdLoginDialog(navController)
}
@Preview(showBackground = true)
@Composable
fun registeredIdDiaglog() {
    val navController = rememberNavController()
    LoginView().registeredIdDialog(navController)
}

