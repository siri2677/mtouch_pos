package com.example.mtouchpos.view.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.mtouchpos.R
import java.io.Serializable

data class SelectDialog(
    val title: String,
    val list: List<String>,
    val initValue: String,
    val onTextChange: (String) -> Unit
): Serializable

@Composable
fun ItemListDialog(
    navController: NavController,
    selectDialog: SelectDialog
) {
    var selectedValue by remember { mutableStateOf(selectDialog.initValue) }
    Dialog(
        onDismissRequest = { navController.popBackStack() }
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
                text = selectDialog.title,
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
                    selectDialog.list.mapIndexed { index, gridItem ->
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
                    navController.popBackStack()
                    selectDialog.onTextChange(selectedValue)
                },
                fontSize = 16.sp,
                roundedCornerShapeSize = 0
            )
        }
    }
}

@Composable
fun ErrorDialog(
    message: String,
    navController: NavController
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    Dialog(
        onDismissRequest = {
            navController.popBackStack()
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
                onClick = { navController.popBackStack() },
                fontSize = 16.sp,
                roundedCornerShapeSize = 0
            )
        }
    }
}

@Composable
fun LoadingDialog(navController: NavController) {
    Dialog(
        onDismissRequest = { navController.popBackStack() }
    ) {
        Column(
            modifier = Modifier
                .width((LocalConfiguration.current.screenWidthDp * 0.3).dp)
                .height(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                modifier = Modifier
                    .padding(bottom = 15.dp)
                    .clip(RoundedCornerShape(percent = 50)),
                painter = painterResource(id = R.drawable.img_1),
                contentDescription = "mtocuIcon"
            )
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = colorResource(id = R.color.watermelon)
            )
        }
    }
}
