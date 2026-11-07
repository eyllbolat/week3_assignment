package com.example.week3_assignment.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.week3_assignment.ui.home.components.DeviceItem
import com.example.week3_assignment.ui.home.components.EnergyCard
import com.example.week3_assignment.ui.home.components.RoomFilterRow
import com.example.week3_assignment.ui.home.components.SearchBar

/**
 * Route: ViewModel'i bilen TEK composable.
 * State'i lifecycle-aware toplar, event'leri lambda olarak aşağı iletir.
 */
@Composable
fun HomeRoute(
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onRoomSelected = viewModel::onRoomSelected,
        onToggleDevice = viewModel::onToggleDevice
    )
}

/**
 * STATELESS ekran: sadece parametre alır, çizer, event'i yukarı bildirir.
 * İçinde remember / filter / sum / hesaplama yoktur.
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRoomSelected: (String?) -> Unit,
    onToggleDevice: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Akıllı Ev Kontrol Merkezi",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp)
            )

            EnergyCard(
                totalPowerText = uiState.totalPowerText,
                activeDeviceCountText = uiState.activeDeviceCountText
            )

            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange
            )

            RoomFilterRow(
                filters = uiState.roomFilters,
                onRoomSelected = onRoomSelected
            )

            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                uiState.showEmptyMessage -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("Aramanıza uygun cihaz bulunamadı.") }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.devices,
                        key = { it.id } // sabit key → sadece değişen satır yeniden çizilir
                    ) { device ->
                        DeviceItem(
                            device = device,
                            onToggle = onToggleDevice
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        uiState = HomeUiState(
            isLoading = false,
            totalPowerText = "106,0 W",
            activeDeviceCountText = "6 cihaz aktif",
            roomFilters = listOf(
                RoomFilterUiModel("Tümü", null, true),
                RoomFilterUiModel("Salon", "Salon", false)
            ),
            devices = listOf(
                DeviceUiModel(1, "Köşe Lambası", "Salon • Aydınlatma", "15,0 W", "Açık", true, true, "Kapat", true),
                DeviceUiModel(9, "Balkon Aydınlatması", "Balkon • Aydınlatma", "25,0 W", "Çevrimdışı", false, false, "Aç", false)
            )
        ),
        searchQuery = "",
        onSearchQueryChange = {},
        onRoomSelected = {},
        onToggleDevice = {}
    )
}
