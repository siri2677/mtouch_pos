package com.example.cleanarchitech_text_0506.view.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cleanarchitech_text_0506.R
import com.example.cleanarchitech_text_0506.enum.NavigationView

data class MainGridItem(val imageRes: Int, val text: String, val navigator: String)
data class GridItem(val imageRes: Int, val text: String)
class LoginView {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun pgIdLoginDialog(navHostController: NavHostController) {
        val screenWidth = LocalConfiguration.current.screenWidthDp
        var text by remember { mutableStateOf("") }
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
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("가맹점 ID") },
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(top = 10.dp)
                    )
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("터미널 ID") },
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(top = 5.dp)
                    )
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("시리얼 번호") },
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(top = 5.dp)
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
    fun registeredIdDialog(navHostController: NavHostController) {
        val screenWidth = LocalConfiguration.current.screenWidthDp
        var text by remember { mutableStateOf("") }
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
                        val gridItems = listOf(
                            GridItem(R.drawable.card_icon_main, "test0003"),
                            GridItem(R.drawable.receipt_icon, "test0005"),
                            GridItem(R.drawable.payment_icon, "TMN111112"),
                            GridItem(R.drawable.payment_icon, "TMN010101"),
                            GridItem(R.drawable.payment_icon, "TMN939393"),
                            GridItem(R.drawable.payment_icon, "TMN123456"),
                            GridItem(R.drawable.payment_icon, "TMN990083"),
                        )
                        item {
                            gridItems.forEachIndexed { index, gridItem ->
                                registeredIdList("PG", gridItem, screenWidth)
                            }
                        }
                    }
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
    fun registeredIdList(index: String, gridItems: GridItem, screenWidth: Int) {
        var selected by remember { mutableStateOf(false) }
        val color = if (selected) colorResource(id = R.color.grey5) else Color.White
        Button(
            modifier = Modifier.width((screenWidth * 0.7).dp),
            onClick = { selected = !selected },
            border = BorderStroke(1.dp, Color.Black),
            shape = RoundedCornerShape(10),
            colors = ButtonDefaults.buttonColors(containerColor = color)
        ) {
            Row() {
                Text(
                    text = index.toString(),
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(0.2f)
                )
                Text(
                    text = gridItems.text,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.7f)
                )
                Text(
                    text = "x",
                    color = Color.Black,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.1f)
                )
            }
        }
    }

    @Composable
    fun loginDialog(navHostController: NavHostController) {
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
                            .padding(vertical = 10.dp)
                            .clickable(onClick = { navHostController.navigate(NavigationView.PgIdLogin.name) }),
                        fontSize = 16.sp
                    )
                    GradientButton(
                        text = "VANID 로그인",
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(vertical = 10.dp)
                            .clickable(onClick = { navHostController.navigate(NavigationView.VanIdLogin.name) }),
                        fontSize = 16.sp
                    )
                    GradientButton(
                        text = "등록된 아이디 조회",
                        modifier = Modifier
                            .width((screenWidth * 0.7).dp)
                            .padding(vertical = 10.dp)
                            .clickable(onClick = { navHostController.navigate(NavigationView.RegisteredId.name) }),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    @Composable
    fun GradientButton(
        text: String,
        modifier: Modifier = Modifier,
        onClick: () -> Unit = { },
        fontSize: TextUnit
    ) {
        Button(
            modifier = modifier,
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            onClick = { onClick() },
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colorResource(id = R.color.orange_pink),
                                colorResource(id = R.color.watermelon)
                            )
                        )
                    )
                    .then(modifier),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = text,
                    fontSize = fontSize
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun loginDiaglog() {
    val navController = rememberNavController()
    LoginView().loginDialog(navController)
}
@Preview(showBackground = true)
@Composable
fun pgloginDiaglog() {
    val navController = rememberNavController()
    LoginView().pgIdLoginDialog(navController)
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
