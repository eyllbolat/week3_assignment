package com.example.week3_assignment.data

import com.example.week3_assignment.data.model.Device
import com.example.week3_assignment.data.model.DeviceCategory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * Veri katmanı (Single Source of Truth).
 * Gerçek projede burası Room DB / REST API olurdu; burada bellek içi veri
 * ve gecikme ile simüle ediyoruz. Tüm I/O işleri Dispatchers.IO üzerinde yapılır.
 */
class DeviceRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices.asStateFlow()

    /** Cihazları "sunucudan" yükler (IO thread). */
    suspend fun loadDevices() = withContext(ioDispatcher) {
        delay(400) // Ağ / veritabanı gecikmesi simülasyonu
        _devices.value = INITIAL_DEVICES
    }

    /**
     * Cihazın Aç/Kapat durumunu değiştirir (IO thread).
     * Offline cihazlar değiştirilemez.
     * update{} atomiktir; hızlı art arda tıklamalarda veri kaybı olmaz.
     */
    suspend fun toggleDevice(deviceId: Int) = withContext(ioDispatcher) {
        _devices.update { list ->
            list.map { device ->
                if (device.id == deviceId && device.isOnline) {
                    device.copy(isActive = !device.isActive)
                } else {
                    device
                }
            }
        }
    }

    private companion object {
        val INITIAL_DEVICES = listOf(
            Device(1, "Köşe Lambası", DeviceCategory.LIGHT, "Salon", isOnline = true, isActive = true, powerWatt = 15.0),
            Device(2, "Tavan Avizesi", DeviceCategory.LIGHT, "Salon", isOnline = true, isActive = false, powerWatt = 60.0),
            Device(3, "Bahçe Kamerası", DeviceCategory.CAMERA, "Bahçe", isOnline = true, isActive = true, powerWatt = 8.5),
            Device(4, "Giriş Kamerası", DeviceCategory.CAMERA, "Antre", isOnline = true, isActive = true, powerWatt = 8.5),
            Device(5, "Merkezi Termostat", DeviceCategory.THERMOSTAT, "Salon", isOnline = true, isActive = true, powerWatt = 12.0),
            Device(6, "Yatak Odası Kliması", DeviceCategory.THERMOSTAT, "Yatak Odası", isOnline = true, isActive = false, powerWatt = 1200.0),
            Device(7, "Mutfak Spotları", DeviceCategory.LIGHT, "Mutfak", isOnline = true, isActive = false, powerWatt = 45.0),
            Device(8, "Roborock S7", DeviceCategory.VACUUM, "Koridor", isOnline = true, isActive = true, powerWatt = 50.0),
            Device(9, "Balkon Aydınlatması", DeviceCategory.LIGHT, "Balkon", isOnline = false, isActive = false, powerWatt = 25.0),
            Device(10, "Çalışma Odası Lambası", DeviceCategory.LIGHT, "Ofis", isOnline = true, isActive = true, powerWatt = 12.0)
        )
    }
}
