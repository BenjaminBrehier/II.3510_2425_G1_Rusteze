package com.bl.rustyze.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp) // Ensure consistent height for the top bar
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart) // Align the Row to the top of the Box
                .padding(horizontal = 16.dp, vertical = 50.dp), // Adjust padding as needed
            verticalAlignment = Alignment.CenterVertically, // Align items within the Row vertically
            horizontalArrangement = Arrangement.SpaceBetween // Space out elements
        ) {
            // Search Bar - takes up most of the space
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Search") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f) // Makes the search bar take up most of the space
                    .height(40.dp), // Ensure the search bar height matches the profile button
                textStyle = TextStyle(fontSize = 16.sp) // Adjusts font size for readability
            )

            // Profile Button at the end
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .size(56.dp) // Larger size for the profile button to make it prominent
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.size(40.dp) // Icon size within the button
                )
            }
        }
    }
}