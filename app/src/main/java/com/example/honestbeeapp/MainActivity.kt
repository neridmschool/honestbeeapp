package com.example.honestbeeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.honestbeeapp.navigation.AppNavigation
import com.example.honestbeeapp.ui.theme.HonestbeeAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HonestbeeAppTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                AppNavigation()
            }
        }
    }
}
