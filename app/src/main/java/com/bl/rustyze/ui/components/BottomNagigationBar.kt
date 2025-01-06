package com.bl.rustyze.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BottomNavigationBar(onHomeClick: () -> Unit, onSavedClick: () -> Unit, onUpdatesClick: () -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = onHomeClick,
            label = { Text("Home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onSavedClick,
            label = { Text("Saved") },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Saved") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onUpdatesClick,
            label = { Text("Updates") },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Updates") }
        )
    }
}
