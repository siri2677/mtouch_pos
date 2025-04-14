package com.kwonps.mtouchpos.view.ui

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kwonps.mtouchpos.R
import com.kwonps.mtouchpos.coordinator.LoginCoordinator
import com.kwonps.mtouchpos.view.util.GradientButton
import com.kwonps.mtouchpos.viewmodel.LoginVM
import com.kwonps.mtouchpos.vo.info.UserInfo
import com.kwonps.mtouchpos.vo.type.UseCaseResult

@Composable
fun PgIdLoginDialog(
    navController: NavController,
    loginViewModel: LoginVM = hiltViewModel()
) {
    data class TextBox(
        val title: String,
        val default: String,
        val edit: (String) -> UserInfo
    )

    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val loginInfo = loginViewModel.loginInfo.collectAsStateWithLifecycle().value

    LoginCoordinator(navController).ObserveResultLogin(
        context = context,
        reactLogin = loginViewModel.reactLogin
            .collectAsStateWithLifecycle(UseCaseResult.Init).value
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

                loginViewModel.loginInfo.run {
                    listOf(
                        TextBox("가맹점 ID", loginInfo.mchtId) { value.copy(mchtId = it) },
                        TextBox("터미널 ID", loginInfo.tmnId) { value.copy(tmnId = it) },
                        TextBox("시리얼 번호", loginInfo.serial) { value.copy(serial = it) },
                    )
                }.forEachIndexed { index, textFieldInfo ->
                    OutlinedTextField(
                        value = textFieldInfo.default,
                        onValueChange = { loginViewModel.updateUserInfo(textFieldInfo.edit(it)) },
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
                    onClick = { loginViewModel.login() },
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun RegisteredIdDialog(
    navController: NavController,
    loginViewModel: LoginVM = hiltViewModel()
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val context = LocalContext.current

    val reactLogin = loginViewModel.reactLogin.collectAsStateWithLifecycle(UseCaseResult.Init).value
    val userInfo = loginViewModel.userInfo.collectAsStateWithLifecycle(emptyList()).value
    val loginInfo = loginViewModel.loginInfo.collectAsStateWithLifecycle().value
    var selectedIndex by remember { mutableIntStateOf(-2) }

    LoginCoordinator(navController).ObserveResultLogin(
        context = context,
        reactLogin = reactLogin
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
                    Text("등록된 터미널 아이디 조회", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                LazyColumn(
                    modifier = Modifier
                        .height(300.dp)
                        .padding(top = 10.dp),
                ) {
                    item {
                        userInfo.forEachIndexed { index, gridItem ->
                            RegisteredIdList(
                                tmnId = gridItem.tmnId,
                                isSelected = selectedIndex == index,
                                onItemTap = {
                                    if(selectedIndex == index) {
                                        selectedIndex = -1
                                        loginViewModel.updateUserInfo(
                                            loginInfo.copy(
                                                mchtId = "",
                                                tmnId = "",
                                                serial = ""
                                            )
                                        )
                                    } else {
                                        selectedIndex = index
                                        loginViewModel.updateUserInfo(
                                            loginInfo.copy(
                                                mchtId = gridItem.mchtId,
                                                tmnId = gridItem.tmnId,
                                                serial = gridItem.serial
                                            )
                                        )
                                    }
                                },
                                onDeleteTap = { loginViewModel.deleteUserInfo(gridItem.tmnId) }
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
                    onClick = { loginViewModel.login() }
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
                    .clickable(onClick = { onDeleteTap() }),
                color = Color.Black,
                textAlign = TextAlign.End,
            )
        }
    }
}