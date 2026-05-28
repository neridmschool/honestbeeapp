package com.example.honestbeeapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.honestbeeapp.R

@Composable
fun HonestbeeLogo(
    modifier: Modifier = Modifier,
    contentDescription: String = "honestbee logo"
) {
    Image(
        painter = painterResource(id = R.drawable.honestbee_logo),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
