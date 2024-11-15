package com.example.mtouchpos.view.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mtouchpos.R

@Composable
fun ColumnKeyValueTextBox(
    modifier: Modifier = Modifier,
    key: String,
    value: String,
    keyFontSize: TextUnit = 17.sp,
    valueFontSize: TextUnit = 17.sp
) {
    Column(
        modifier = Modifier
            .then(modifier)
    ) {
        Text(
            text = key,
            fontSize = keyFontSize,
            color = colorResource(id = R.color.grey2)
        )
        Text(
            text = value,
            fontSize = valueFontSize
        )
    }
}

@Composable
fun RowSmallSizeTextBox(
    modifier: Modifier = Modifier,
    value: String
) {
    Column(
        modifier = Modifier
            .height(42.dp)
            .padding(start = 10.dp)
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            text = value,
            lineHeight = 18.sp,
            color = colorResource(id = R.color.white),
        )
    }
}

@Composable
fun GradientButton(
    onClick: () -> Unit = { },
    roundedCornerShapeSize: Int = 6,
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit
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