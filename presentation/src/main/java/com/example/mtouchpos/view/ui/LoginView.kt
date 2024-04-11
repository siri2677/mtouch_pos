package com.example.mtouchpos.view.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import com.example.mtouchpos.R
import com.example.mtouchpos.dto.LoginInfo
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.util.GradientButton
import com.example.mtouchpos.view.ui.theme.ObserveResultLogin
import com.example.mtouchpos.viewmodel.MainActivityViewModel

@Composable
fun PgIdLoginDialog(
    navController: NavController,
    mainActivityViewModel: MainActivityViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp
    var merchantId by remember { mutableStateOf("") }
    var terminalId by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }

    ObserveResultLogin(
        mainActivityViewModel = mainActivityViewModel,
        navController = navController,
        afterProcess = {
            Toast.makeText(context, "로그인이 완료되었습니다", Toast.LENGTH_SHORT).show()
            navController.navigate(
                route = NavigationGraphState.HomeView.Home.name,
                navOptions = NavOptions.Builder().setRestoreState(true).build()
            )
        }
    )

    Dialog(
        onDismissRequest = { navController.popBackStack() }
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
                data class TextFieldVO(
                    val title: String,
                    val default: String,
                    val edit: (String) -> Unit
                )
                mutableListOf(
                    TextFieldVO("가맹점 ID", merchantId) { merchantId = it },
                    TextFieldVO("터미널 ID", terminalId) { terminalId = it },
                    TextFieldVO("시리얼 번호", serialNumber) { serialNumber = it },
                ).mapIndexed  { index, textFieldInfo ->
                    OutlinedTextField(
                        value = textFieldInfo.default,
                        onValueChange = { newValue ->
                            textFieldInfo.edit(newValue)
                        },
                        label = { Text(textFieldInfo.title) },
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(top = if (index == 0) 10.dp else 5.dp)
                    )
                }
                GradientButton(
                    text = "로그인",
                    modifier = Modifier
                        .width((screenWidth * 0.7).dp)
                        .padding(top = 12.dp),
                    onClick = {
                        mainActivityViewModel.login(
                            LoginInfo(
                                mchtId = merchantId,
                                tmnId = terminalId,
                                serial = serialNumber
                            )
                        )
                    },
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun VanIdLoginDialog(navController: NavController) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    var text by remember { mutableStateOf("") }
    Dialog(
        onDismissRequest = { navController.popBackStack() }
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
fun RegisteredIdDialog(
    navController: NavController,
    mainActivityViewModel: MainActivityViewModel = hiltViewModel()
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val context = LocalContext.current
    var selectedIndex by remember { mutableIntStateOf(-2) }
    var tapItem by remember {
        mutableStateOf(
            LoginInfo(
                tmnId = "",
                serial = "",
                mchtId = ""
            )
        )
    }
    var userInformationList by remember { mutableStateOf(mainActivityViewModel.getUserInformationRepository()) }

    ObserveResultLogin(
        mainActivityViewModel = mainActivityViewModel,
        navController = navController,
        afterProcess = {
            Toast.makeText(context, "로그인이 완료되었습니다", Toast.LENGTH_SHORT).show()
            navController.navigate(
                route = NavigationGraphState.HomeView.Home.name,
                navOptions = NavOptions.Builder().setRestoreState(true).build()
            )
        }
    )

    Dialog(
        onDismissRequest = { navController.popBackStack() }
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
                            RegisteredIdList(
                                tmnId = gridItem.tmnId!!,
                                isSelected = selectedIndex == index,
                                onItemTap = {
                                    selectedIndex = index
                                    tapItem = gridItem
                                },
                                onDeleteTap = {
                                    mainActivityViewModel.deleteUserInformationRepository(gridItem.tmnId!!)
                                    userInformationList =
                                        mainActivityViewModel.getUserInformationRepository()
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
                        mainActivityViewModel.login(
                            LoginInfo(
                                mchtId = tapItem.mchtId,
                                tmnId = tapItem.tmnId,
                                serial = tapItem.serial
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun RegisteredIdList(
    isSelected: Boolean,
    onItemTap: () -> Unit,
    onDeleteTap: () -> Unit,
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
fun LoginDialog(
    navController: NavController
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    Dialog(
        onDismissRequest = { navController.popBackStack() }
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
                data class GradientButtonVO(
                    val text: String,
                    val navigate: String
                )
                
                mutableListOf(
                    GradientButtonVO("PGID 로그인", NavigationGraphState.HomeView.PgIdLogin.name),
                    GradientButtonVO("VANID 로그인", NavigationGraphState.HomeView.VanIdLogin.name),
                    GradientButtonVO("등록된 아이디 조회", NavigationGraphState.HomeView.RegisteredId.name),
                ).map {
                    GradientButton(
                        text = it.text,
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(vertical = 10.dp),
                        onClick = { navController.navigate(it.navigate) },
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun loginDiaglog() {
    val context = LocalContext.current
    val navController = rememberNavController()
    LoginDialog(navController)
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
    VanIdLoginDialog(navController)
}

@Preview(showBackground = true)
@Composable
fun registeredIdDiaglog() {
    val navController = rememberNavController()
    RegisteredIdDialog(navController)
}

