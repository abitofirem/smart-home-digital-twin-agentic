package com.example.iothome.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ********** ÖNEMLİ DÜZELTME BAŞLANGICI **********
// Sizin Color.kt dosyanızdan tüm sabitleri otomatik olarak alabilmek için
// herhangi bir manuel 'import com.example.iothome.ui.theme.RenkAdı' satırı eklemenize gerek YOKTUR,
// çünkü hepsi aynı paketin (com.example.iothome.ui.theme) içindedir.

// ----------------------------------------------------
// KOYU TEMA RENK PALETİ
// ----------------------------------------------------
private val DarkColorScheme = darkColorScheme(
    // Yüzey ve Kart Renkleri
    background = BackgroundDark,        // #12121D - Koyu Arka Plan
    surface = SurfaceDark,              // #1E1E2C - Kart Arka Planı
    onBackground = OnSurfaceDark,       // #EBEBF5 - Metin
    onSurface = OnSurfaceDark,          // Kart Üzerindeki Metin

    // Vurgu Renkleri
    primary = PrimaryPurple,            // #6F42C1 - Ana Vurgu (Mor)
    onPrimary = OnPrimaryWhite,         // Beyaz Metin

    // İkincil Vurgu (Eski 'AppSecondary' yerine şimdilik mor/gri tonlarını kullanıyoruz)
    // Eğer görseldeki Canlı Mavi/Cyan'ı kullanmak isterseniz:
    secondary = PurpleGrey80,           // Geçici/Varsayılan M3 rengi.

    // Hata ve Termostat Kırmızısı
    error = GraphHot                    // #F04D65 - Hata veya Sıcak Durum
)

// ----------------------------------------------------
// IŞIK TEMA RENK PALETİ
// ----------------------------------------------------
private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,            // Ana Vurgu Mor
    onPrimary = OnPrimaryWhite,
    background = Color.White,
    surface = Color(0xFFF0F0F5),
    onBackground = Color.Black,
    onSurface = Color.Black,

    // İkincil Vurgu
    secondary = PurpleGrey40,

    error = GraphHot
)


@Composable
fun MobileappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic Color devre dışı
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Typography dosyanızın tanımlı olduğunu varsayıyoruz
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Typography dosyanız burada olmalı
        content = content
    )
}