package com.example.iothome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.iothome.ui.screens.home.HomeScreen
import com.example.iothome.ui.theme.MobileappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MobileappTheme { // Tema adı
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation() // Ana gezintiyi çağır
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    // Sadece Home ekranımızı yüklüyoruz
    HomeScreen()
}

