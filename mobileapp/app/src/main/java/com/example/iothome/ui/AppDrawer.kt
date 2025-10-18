package com.example.iothome.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.OnlinePrediction
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.iothome.ui.navigation.DrawerItem
import com.example.iothome.ui.navigation.drawerItems
import com.example.iothome.ui.theme.MobileappTheme
import com.example.iothome.ui.theme.PrimaryPurple // Tema Rengi

// Not: Bu, Drawer'ın açıldığında görünen içeriğidir.

@Composable
fun AppDrawer(
    navController: NavHostController,
    closeDrawer: () -> Unit // Menüyü kapatma fonksiyonu
) {
    // Drawer'ın arka planı için tema renginizi kullanın
    val drawerContainerColor = MaterialTheme.colorScheme.surface
    val drawerContentColor = MaterialTheme.colorScheme.onSurface

    ModalDrawerSheet(
        drawerContainerColor = drawerContainerColor,
        modifier = Modifier.width(300.dp)
    ) {
        // --- 1. HEADER: Profil Özeti ve Durum ---
        DrawerHeader(name = "Müşteri Kullanıcısı", status = "Online")

        // --- 2. Çekirdek Yönetim Menü Öğeleri ---
        Spacer(Modifier.height(16.dp))
        drawerItems.forEach { item ->
            DrawerMenuItem(
                item = item,
                navController = navController,
                closeDrawer = closeDrawer,
                isSelected = navController.currentDestination?.route == item.route
            )
        }

        // --- 3. FOOTER: Ayarlar ve Çıkış ---
        Spacer(Modifier.weight(1f)) // Öğeleri aşağı iter
        Divider(color = drawerContentColor.copy(alpha = 0.1f))

        // Tema Değiştirme (Switch) - Henüz fonksiyonel değil, sadece görsel
        ThemeToggleItem()

        // Hakkında ve Çıkış
        DrawerMenuItem(
            item = DrawerItem.About,
            navController = navController,
            closeDrawer = closeDrawer,
            isSelected = navController.currentDestination?.route == DrawerItem.About.route
        )
        ExitItem() // Çıkış Yap butonu
    }
}

// Drawer Header Bileşeni
@Composable
fun DrawerHeader(name: String, status: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Profil Fotoğrafı (Yuvarlak Placeholder)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(PrimaryPurple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text("İ", style = MaterialTheme.typography.headlineMedium, color = PrimaryPurple)
        }
        Spacer(Modifier.height(12.dp))

        // Kullanıcı Adı
        Text(name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)

        // MQTT Bağlantı Durumu
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (status == "Online") Color.Green else Color.Red)
            )
            Spacer(Modifier.width(8.dp))
            Text("MQTT $status", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

// Menü Öğesi Bileşeni
@Composable
fun DrawerMenuItem(
    item: DrawerItem,
    navController: NavHostController,
    closeDrawer: () -> Unit,
    isSelected: Boolean
) {
    NavigationDrawerItem(
        label = { Text(item.label) },
        icon = { Icon(item.icon, contentDescription = item.label) },
        selected = isSelected,
        onClick = {
            navController.navigate(item.route)
            closeDrawer()
        },
        // Seçili öğenin arka planını vurgu renginizle hafifçe renklendirin
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = PrimaryPurple.copy(alpha = 0.1f),
            selectedIconColor = PrimaryPurple,
            selectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

// Çıkış Yap Butonu
@Composable
fun ExitItem() {
    NavigationDrawerItem(
        label = { Text("Çıkış Yap") },
        icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Çıkış") },
        selected = false,
        onClick = { /* Oturumu Kapatma Fonksiyonu */ },
        colors = NavigationDrawerItemDefaults.colors(
            unselectedIconColor = Color.Red,
            unselectedTextColor = Color.Red
        ),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
    )
}

// Tema Değiştirme Öğesi
@Composable
fun ThemeToggleItem() {
    // Şimdilik sadece görsel, gerçek mantık ViewModel'a eklenecektir.
    var isDark by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Koyu Tema", color = MaterialTheme.colorScheme.onSurface)
        Switch(
            checked = isDark,
            onCheckedChange = { isDark = it /* Temayı değiştirme ViewModel fonksiyonu */ }
        )
    }
}

@Preview
@Composable
fun AppDrawerPreview() {
    // Preview için sahte NavController ve kapatma fonksiyonu
    val previewNavController = rememberNavController()
    val mockClose: () -> Unit = {} // Boş bir fonksiyon

    // Uygulamanın koyu temasını çağırıyoruz
    MobileappTheme(darkTheme = true) {
        // AppDrawer'ı görüntülüyoruz
        AppDrawer(
            navController = previewNavController,
            closeDrawer = mockClose
        )
    }
}