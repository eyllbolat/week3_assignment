package com.example.week3_assignment.ui.home

/**
 * Ekranın TEK durum nesnesi. UI sadece bunu okur ve çizer.
 * Tüm metinler, toplamlar ve seçili/aktif bilgileri ViewModel'de hazırlanır;
 * böylece Compose katmanında hiçbir hesaplama / filtreleme kalmaz.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val totalPowerText: String = "0,0 W",
    val activeDeviceCountText: String = "",
    val roomFilters: List<RoomFilterUiModel> = emptyList(),
    val devices: List<DeviceUiModel> = emptyList(),
    val showEmptyMessage: Boolean = false
)

/** Oda filtre çipi. room == null → "Tümü" çipi. */
data class RoomFilterUiModel(
    val label: String,
    val room: String?,
    val isSelected: Boolean
)

/** Listede bir satırın gösterim modeli (UI'a hazır). */
data class DeviceUiModel(
    val id: Int,
    val name: String,
    val subtitle: String,       // "Salon • Aydınlatma"
    val powerText: String,      // "15,0 W"
    val statusText: String,     // "Açık" / "Kapalı" / "Çevrimdışı"
    val isActive: Boolean,
    val isOnline: Boolean,
    val buttonText: String,     // "Kapat" / "Aç"
    val canToggle: Boolean      // offline ise buton pasif
)
