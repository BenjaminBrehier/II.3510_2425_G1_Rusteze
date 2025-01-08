package com.bl.rustyze.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bl.rustyze.data.models.Vehicle

@Composable
fun VehicleDetailScreen(vehicle: Vehicle) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "${vehicle.make} ${vehicle.model}",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VehicleDetailItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Year",
                    value = vehicle.year.toString()
                )
                VehicleDetailItem(
                    icon = Icons.Default.Done,
                    label = "Fuel Type",
                    value = vehicle.fuelType ?: "Unknown"
                )
                VehicleDetailItem(
                    icon = Icons.Default.Lock,
                    label = "Cylinders",
                    value = vehicle.cylinders?.toString() ?: "N/A"
                )
                VehicleDetailItem(
                    icon = Icons.Default.Settings,
                    label = "Transmission",
                    value = vehicle.trany ?: "N/A"
                )
                VehicleDetailItem(
                    icon = Icons.Default.Call,
                    label = "Combined MPG",
                    value = vehicle.comb08?.toString() ?: "N/A"
                )
            }
        }
    }
}

@Composable
fun VehicleDetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
