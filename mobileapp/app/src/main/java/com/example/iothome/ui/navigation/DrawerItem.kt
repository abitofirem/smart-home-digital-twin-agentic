package com.example.iothome.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// Yan Menü Öğelerinin Tanımı
sealed class DrawerItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    // 1. Yönetim ve Yapılandırma
    object DeviceCatalog : DrawerItem("device_catalog_route", Icons.Default.Inventory, "Cihaz Kataloğu")
    object AIFlow : DrawerItem("ai_rules_route", Icons.Default.Rule, "AI Kuralları")
    object Diagnostics : DrawerItem("diagnostics_route", Icons.Default.BugReport, "Loglar & Tanılama")

    // 2. Uygulama ve Kullanıcı İşlevleri
    object AppSettings : DrawerItem("settings_main_route", Icons.Default.Tune, "Uygulama Ayarları")
    object About : DrawerItem("about_route", Icons.Default.Info, "Hakkında")
}

// Bu liste, menü öğelerinin hiyerarşik sırasını belirler.
val drawerItems = listOf(
    DrawerItem.DeviceCatalog,
    DrawerItem.AIFlow,
    DrawerItem.Diagnostics,

    // Uygulama Ayarları ve Hakkında öğeleri de artık listede!
    DrawerItem.AppSettings,
    DrawerItem.About
)