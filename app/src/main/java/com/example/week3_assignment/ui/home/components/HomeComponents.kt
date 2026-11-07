package com.example.week3_assignment.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.week3_assignment.ui.home.DeviceUiModel
import com.example.week3_assignment.ui.home.RoomFilterUiModel

/** En üstteki canlı enerji sayacı kartı. Değer ViewModel'den hazır gelir. */
@Composable
fun EnergyCard(
    totalPowerText: String,
    activeDeviceCountText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Anlık Toplam Tüketim",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = totalPowerText,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = activeDeviceCountText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/** Arama barı — değeri ve değişimi tamamen dışarıdan gelir (state hoisting). */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Cihaz ara") },
        placeholder = { Text("ör. Lamba, Kamera...") }
    )
}

/** Oda filtre çipleri. Hangi çipin seçili olduğu ViewModel'de hesaplanır. */
@Composable
fun RoomFilterRow(
    filters: List<RoomFilterUiModel>,
    onRoomSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = filters, key = { it.label }) { filter ->
            FilterChip(
                selected = filter.isSelected,
                onClick = { onRoomSelected(filter.room) },
                label = { Text(filter.label) }
            )
        }
    }
}

/** Tek cihaz satırı + Aç/Kapat butonu. */
@Composable
fun DeviceItem(
    device: DeviceUiModel,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (device.isActive) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = device.subtitle,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${device.powerText} • ${device.statusText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (device.isOnline) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            if (device.isActive) {
                Button(
                    onClick = { onToggle(device.id) },
                    enabled = device.canToggle
                ) { Text(device.buttonText) }
            } else {
                OutlinedButton(
                    onClick = { onToggle(device.id) },
                    enabled = device.canToggle
                ) { Text(device.buttonText) }
            }
        }
    }
}
