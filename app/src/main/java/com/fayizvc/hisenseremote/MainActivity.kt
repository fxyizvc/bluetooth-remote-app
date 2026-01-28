package com.fayizvc.hisenseremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fayizvc.hisenseremote.ui.theme.HisenseRemoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HisenseRemoteTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RemoteLayout(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun RemoteLayout(modifier: Modifier = Modifier) {
    // The main container for the remote
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)) // Dark grey background like the remote
            .verticalScroll(rememberScrollState()) // Allow scrolling if screen is small
            .padding(24.dp), // Added slightly more padding for look
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(30.dp) // Space between major sections
    ) {
        // --- Section 1: Header (Power, Input, etc) ---
        TopSection()

        // --- Section 2: Navigation (The Circle) ---
        // Placeholder for now
        Box(
            modifier = Modifier
                .size(220.dp)
                .background(Color.DarkGray, CircleShape)
        ) {
            Text("Navigation Pad", color = Color.White, modifier = Modifier.align(Alignment.Center))
        }

        // --- Section 3: Vol/Ch ---
        Text(text = "Volume / Channel / Home", color = Color.Gray)

        // --- Section 4: App Buttons ---
        Text(text = "Netflix / YouTube / Media", color = Color.Gray)
    }
}

@Composable
fun TopSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Row 1: Power (Left) and Input (Right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Power Button (Red)
            RemoteButton(
                icon = Icons.Filled.PowerSettingsNew,
                contentDescription = "Power",
                tint = Color.Red,
                backgroundColor = Color(0xFF2D2D2D)
            )

            // Input Button
            RemoteButton(
                icon = Icons.Filled.Input,
                contentDescription = "Input",
                backgroundColor = Color(0xFF2D2D2D)
            )
        }

        // Row 2: Profile - Assistant - Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile (Small)
            RemoteButton(
                icon = Icons.Filled.Person,
                contentDescription = "Profile",
                size = 40.dp
            )

            // Google Assistant (Big White)
            RemoteButton(
                icon = Icons.Filled.Mic,
                contentDescription = "Assistant",
                backgroundColor = Color.White, // White background
                tint = Color.Black,          // Black icon
                size = 64.dp                 // Prominent size
            )

            // Settings (Small)
            RemoteButton(
                icon = Icons.Filled.Settings,
                contentDescription = "Settings",
                size = 40.dp
            )
        }
    }
}

// Reusable Button Component
@Composable
fun RemoteButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF2D2D2D), // Default dark grey
    tint: Color = Color.White,                 // Default white icon
    size: Dp = 48.dp                           // Default size
) {
    IconButton(
        onClick = { /* TODO: Add Bluetooth Logic Later */ },
        modifier = modifier
            .size(size)
            .background(backgroundColor, CircleShape),
        colors = IconButtonDefaults.iconButtonColors(contentColor = tint)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.padding(8.dp) // Padding inside the circle
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RemotePreview() {
    HisenseRemoteTheme {
        RemoteLayout()
    }
}