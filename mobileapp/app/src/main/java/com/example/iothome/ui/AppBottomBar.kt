package com.example.iothome.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.iothome.ui.navigation.BottomNavItem
import com.example.iothome.ui.navigation.bottomNavItems
import com.example.iothome.ui.theme.PrimaryPurple


@Composable
fun AppBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // NavigationBar'ı sarmalayan, koyu arka planlı ve üst köşeleri yuvarlatılmış kutu
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(MaterialTheme.colorScheme.surface) // Kart Arka Planınız
            .padding(vertical = 4.dp, horizontal = 8.dp) // Nav Bar çevresinde boşluk
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // Sabit yükseklik
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { screen ->
                val isSelected = currentRoute == screen.route
                CustomBottomNavItem(
                    screen = screen,
                    isSelected = isSelected,
                    onClick = {
                        navController.navigate(screen.route) {
                            // Navigasyon Kuralları (Eski durumu koru, yığını temizle)
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

/**
 * Gönderdiğiniz görsele uyan oval göstergeli (indicator) özel Bottom Nav Item.
 */
@Composable
fun RowScope.CustomBottomNavItem(
    screen: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    // YENİ: Metnin Opaklık Değerini Animasyonlu Hale Getiriyoruz
    val textAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f, // Seçiliyse 1f, değilse 0f'e geç
        label = "textAlphaAnimation"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple()

    // BÜYÜK OVAL INDICATOR KUTUSU
    Box(
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick, indication = indication, interactionSource = interactionSource)
            .background(if (isSelected) PrimaryPurple.copy(alpha = 0.1f)  else Color.Transparent)
            .animateContentSize(), // Bu, boyut geçişini yumuşatır
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            // 1. İkon
            Icon(
                imageVector = screen.icon,
                contentDescription = screen.label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )

            // 2. Metin (Sadece Seçiliyse ve Opaklığı Yüksekse Gösterilir)
            if (isSelected) {
                Spacer(Modifier.width(4.dp))

                // BURADA DÜZELTME YAPILDI: Opaklık animasyonunu Text bileşenine uyguluyoruz.
                Text(
                    text = screen.label,
                    color = contentColor.copy(alpha = textAlpha), // Animasyonlu opaklık
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    // Eğer metin sığmıyorsa hala problem olabilir, bu yüzden paddingi düşük tuttuk.
                )
            }
        }
    }
}