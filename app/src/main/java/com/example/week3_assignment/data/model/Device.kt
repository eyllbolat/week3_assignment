package com.example.week3_assignment.data.model

/**
 * Cihaz kategorileri. label: ekranda gösterilecek Türkçe ad.
 */
enum class DeviceCategory(val label: String) {
    LIGHT("Aydınlatma"),
    CAMERA("Kamera"),
    THERMOSTAT("Termostat"),
    VACUUM("Robot Süpürge")
}

/**
 * Domain modeli — immutable (val) data class.
 * Durum değişikliği her zaman copy() ile yeni nesne üretilerek yapılır.
 */
data class Device(
    val id: Int,
    val name: String,
    val category: DeviceCategory,
    val room: String,
    val isOnline: Boolean,
    val isActive: Boolean,
    val powerWatt: Double
)
