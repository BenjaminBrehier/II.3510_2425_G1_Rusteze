package com.bl.rustyze.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bl.rustyze.ui.components.CustomTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    Scaffold(
        topBar = {
            CustomTopBar(
                query = "",
                onQueryChange = {},
                onProfileClick = {},
                modifier = Modifier.fillMaxSize()
            )
        },
        bottomBar = {
            NavigationBar {
                Text("Bottom Bar")
            }
        }
    ) {
        Text(
            "Hello, Home Screen!",
            modifier = Modifier.padding(it).fillMaxSize()
        )
    }
}
