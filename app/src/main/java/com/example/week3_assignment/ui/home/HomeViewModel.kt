package com.example.week3_assignment.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.week3_assignment.data.DeviceRepository
import com.example.week3_assignment.data.model.Device
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * MVVM'in "VM" katmanı.
 *  - Girdi (Intent/Event): onSearchQueryChange, onRoomSelected, onToggleDevice
 *  - Çıktı (State): uiState (StateFlow<HomeUiState>), searchQuery (StateFlow<String>)
 *  - Tüm filtreleme + toplama Dispatchers.Default (CPU thread'i) üzerinde yapılır,
 *    Main thread sadece çizim yapar → donma/takılma olmaz.
 *
 * Not: Tüm constructor parametreleri default değerli olduğu için Kotlin
 * parametresiz constructor üretir; viewModel() fabrikası ek ayar olmadan çalışır.
 */
class HomeViewModel(
    private val repository: DeviceRepository = DeviceRepository(),
    private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val trLocale: Locale = Locale.forLanguageTag("tr-TR")

    // --- Kullanıcı girdileri (private mutable, public read-only) ---

    /**
     * Arama metni AYRI bir StateFlow'dur ve Main thread'de senkron güncellenir.
     * TextField'ı arka plan akışına bağlamak imleç zıplamasına / harf kaybına yol açar.
     */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedRoom = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(true)

    /** Her tuşta değil, yazma durduktan kısa süre sonra filtrele. */
    @OptIn(FlowPreview::class)
    private val debouncedQuery = _searchQuery
        .debounce(150)
        .map { it.trim() }
        .distinctUntilChanged()

    // --- Ekran durumu ---

    val uiState: StateFlow<HomeUiState> = combine(
        repository.devices,
        debouncedQuery,
        _selectedRoom,
        _isLoading
    ) { devices, query, selectedRoom, isLoading ->
        buildUiState(devices, query, selectedRoom, isLoading)
    }
        .flowOn(computeDispatcher) // filtre + toplama arka plan thread'inde
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    init {
        viewModelScope.launch {
            try {
                repository.loadDevices()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Eventler (UI → ViewModel) ---

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /** Aynı çipe tekrar basılırsa filtre kalkar ("Tümü"ne döner). */
    fun onRoomSelected(room: String?) {
        _selectedRoom.update { current -> if (current == room) null else room }
    }

    fun onToggleDevice(deviceId: Int) {
        viewModelScope.launch {
            repository.toggleDevice(deviceId) // IO thread'de çalışır
        }
    }

    // --- İş mantığı (UI'da ASLA yer almaz) ---

    private fun buildUiState(
        devices: List<Device>,
        query: String,
        selectedRoom: String?,
        isLoading: Boolean
    ): HomeUiState {
        val normalizedQuery = query.lowercase(trLocale)

        // 1) Arama + oda filtresi
        val filteredDevices = devices.filter { device ->
            val matchesRoom = selectedRoom == null || device.room == selectedRoom
            val matchesQuery = normalizedQuery.isEmpty() ||
                device.name.lowercase(trLocale).contains(normalizedQuery)
            matchesRoom && matchesQuery
        }

        // 2) Reaktif enerji sayacı: filtreden bağımsız, TÜM evdeki online + aktif cihazlar
        val activeOnlineDevices = devices.filter { it.isOnline && it.isActive }
        val totalPower = activeOnlineDevices.sumOf { it.powerWatt }

        // 3) Oda çipleri ("Tümü" + veri setindeki odalar, sırası korunarak)
        val roomFilters = buildList {
            add(RoomFilterUiModel(label = "Tümü", room = null, isSelected = selectedRoom == null))
            devices.map { it.room }.distinct().forEach { room ->
                add(RoomFilterUiModel(label = room, room = room, isSelected = room == selectedRoom))
            }
        }

        return HomeUiState(
            isLoading = isLoading,
            totalPowerText = formatWatt(totalPower),
            activeDeviceCountText = "${activeOnlineDevices.size} cihaz aktif",
            roomFilters = roomFilters,
            devices = filteredDevices.map { it.toUiModel() },
            showEmptyMessage = !isLoading && filteredDevices.isEmpty()
        )
    }

    private fun Device.toUiModel() = DeviceUiModel(
        id = id,
        name = name,
        subtitle = "$room • ${category.label}",
        powerText = formatWatt(powerWatt),
        statusText = when {
            !isOnline -> "Çevrimdışı"
            isActive -> "Açık"
            else -> "Kapalı"
        },
        isActive = isActive,
        isOnline = isOnline,
        buttonText = if (isActive) "Kapat" else "Aç",
        canToggle = isOnline
    )

    private fun formatWatt(value: Double): String =
        String.format(trLocale, "%.1f W", value)
}
