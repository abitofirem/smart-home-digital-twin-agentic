package com.example.iothome.data.model

import androidx.compose.ui.graphics.vector.ImageVector

// 1. Manuel Rutinler (Scenes) için model
data class Routine(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val description: String, // Örn: "3 cihaz açık, ışıklar loş"
    val triggerInfo: String // Örn: "Manuel" veya "Her gün 07:00"
)

// 2. AI Önerileri (Proaktif) için model
data class Suggestion(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val actionCommand: String // "Uygula" butonunun ne yapacağı
)

// 3. (Opsiyonel) Agent Aktivite Günlüğü için model
data class AgentLog(
    val timestamp: String,
    val message: String
)

// AgentScreen'in anlık durumunu tutar
data class AgentUiState(
    // 1. Bağlamsal (En Önemli) Öneri
    val contextualSuggestion: Suggestion? = null,

    // 2. Diğer AI Önerileri
    val suggestions: List<Suggestion> = emptyList(),

    // 3. Kullanıcı Rutinleri
    val routines: List<Routine> = emptyList(),

    // 4. Aktivite Günlüğü
    val logs: List<AgentLog> = emptyList(),

    val isLoading: Boolean = false
)