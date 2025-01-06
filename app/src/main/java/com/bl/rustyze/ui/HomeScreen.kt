package com.bl.rustyze.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bl.rustyze.R
import com.bl.rustyze.ui.components.VehicleCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rustyze") },
                actions = {
                    IconButton(onClick = {
                    }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Home Action */ },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Home") },
                    label = { Text("Home") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Section Top Rated
            item {
                Text(
                    text = "Top Rated",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    content= {
                        LazyRow {
                            items(5) { index ->
                                TopRatedCard(
                                    rank = index + 1,
                                    imageRes = R.drawable.ic_launcher_foreground
                                )
                            }
                        }
                    }
                )
            }

            // Section Seen Recently
            item {
                Text(
                    text = "Seen Recently",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(3) { index ->
                VehicleCard(
                    vehicleName = when (index) {
                        0 -> "Peugeot 1007"
                        1 -> "Moto Guzzi V7"
                        else -> "Seat Leon"
                    },
                    rustyMeter = when (index) {
                        0 -> "20%"
                        1 -> "90%"
                        else -> "60%"
                    }
                )
            }
        }
    }
}

@Composable
fun TopRatedCard(rank: Int, imageRes: Int) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Vehicle Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = "$rank",
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
        )
    }
}
