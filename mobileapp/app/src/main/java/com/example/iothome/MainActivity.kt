package com.example.iothome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.iothome.ui.MainScreen // <-- YENİ IMPORT
import com.example.iothome.ui.theme.MobileappTheme

// import com.example.iothome.ui.screens.home.HomeScreen // <-- HomeScreen artık burada çağrılmaz

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MobileappTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation() // Şimdi bu, MainScreen'i çağıracak.
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    // Düzeltme: Tüm navigasyon mantığını ve Bottom Bar'ı içeren MainScreen'i çağır.
    MainScreen()
}

// ... Diğer Composable'lar ve Preview'lar ...