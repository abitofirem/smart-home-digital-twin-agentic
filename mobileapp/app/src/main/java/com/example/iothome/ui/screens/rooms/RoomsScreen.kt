package com.example.iothome.ui.screens.rooms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.* // Scaffold ve TopAppBar kaldırıldı
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // collectAsState için
import androidx.compose.runtime.collectAsState // collectAsState için
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.iothome.data.model.Device
import com.example.iothome.data.model.DeviceType
import com.example.iothome.data.model.Room
import com.example.iothome.ui.components.RoomCard
import com.example.iothome.ui.screens.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class) // Bu hala gerekli olabilir
@Composable
// Scaffold kaldırıldı, PaddingValues parametresi eklendi
fun RoomsScreen(
    bottomNavController: NavHostController,
    viewModel: HomeViewModel = viewModel(),
    paddingValues: PaddingValues // Ana Scaffold'dan gelen padding
) {
    // Mock veri aynı kalır (ViewModel'dan çekilecek şekilde güncellenecek)
    val mockRooms = listOf(
        Room(
            id = "lroom", name = "Living Room", totalDevices = 5, devicesOn = 2,
            devices = listOf(
                Device(id = "l1", name = "Lamba", type = DeviceType.LIGHT, roomId = "lroom", isOn = true),
                Device(id = "t1", name = "Termostat", type = DeviceType.THERMOSTAT, roomId = "lroom", isOn = false),
                Device(id = "f1", name = "Fan", type = DeviceType.FAN, roomId = "lroom", isOn = true)
            ), imageUrl = "https://havenly.com/blog/wp-content/uploads/2024/02/kyliefitts_havenly_tundeoyeneyin_2-5-1500x970.jpg"
        ),
        Room(id = "bedroom", name = "Bedroom", totalDevices = 4, devicesOn = 0, devices = emptyList(), imageUrl = "https://i.pinimg.com/736x/0c/7c/c9/0c7cc929bf835e6fec415ef9a342cab8.jpg"),
        Room(id = "kitchen", name = "Kitchen", totalDevices = 6, devicesOn = 3, devices = emptyList(), imageUrl = "https://hips.hearstapps.com/hmg-prod/images/f03b864f-2356-405b-bacc-bd408ed20e3c.jpg?crop=1xw:1xh;center,top")
    )

    // Scaffold yerine doğrudan LazyColumn ile başlıyoruz
    LazyColumn(
        modifier = Modifier
            .padding(paddingValues) // ANA SCAFFOLD'DAN GELEN PADDING KULLANILIR
            .fillMaxSize()
            .padding(horizontal = 16.dp), // İçerik için ek yatay padding
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(mockRooms) { room ->
            RoomCard(
                room = room,
                onClick = {
                    // Tıklayınca Oda Detay Ekranına Geçiş
                    //bottomNavController.navigate("room_detail/${room.id}")
                    println("ODA TIKLANDI: Detay navigasyonu devre dışı. Oda ID: ${room.id}")
                },
                onQuickControl = { deviceId ->
                    // Hızlı kontrol lojiki buraya gelecek
                    println("Hızlı Kontrol İsteği: $deviceId")
                },

                )
            // Spacer burada kaldırılabilir, LazyColumn'daki verticalArrangement yeterli
        }
    }
}