package com.fayizvc.hisenseremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.draw.clip
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        // --- Section 1: Header ---
        TopSection()

        // --- Section 2: Navigation (The Real D-Pad) ---
        NavigationPad()

        // --- Section 3: Vol/Ch ---
        Text(text = "Volume / Channel / Home", color = Color.Gray)

        // --- Section 4: App Buttons ---
        Text(text = "Netflix / YouTube / Media", color = Color.Gray)
    }
}

@Composable
fun NavigationPad() {
    // This Box acts as the outer white circle
    Box(
        modifier = Modifier
            .size(220.dp) // Size of the whole D-Pad
            .background(Color(0xFFE0E0E0), CircleShape) // Light grey/white color
    ) {
        // 1. UP Arrow (Aligned Top Center)
        IconButton(
            onClick = { /* TODO: Send UP command */ },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowUp, "Up", tint = Color.Black)
        }

        // 2. DOWN Arrow (Aligned Bottom Center)
        IconButton(
            onClick = { /* TODO: Send DOWN command */ },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowDown, "Down", tint = Color.Black)
        }

        // 3. LEFT Arrow (Aligned Center Start)
        IconButton(
            onClick = { /* TODO: Send LEFT command */ },
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 10.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, "Left", tint = Color.Black)
        }

        // 4. RIGHT Arrow (Aligned Center End)
        IconButton(
            onClick = { /* TODO: Send RIGHT command */ },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowRight, "Right", tint = Color.Black)
        }

        // 5. CENTER OK Button (Aligned Center)
        // We reuse our RemoteButton but make it bigger and Black
        RemoteButton(
            icon = androidx.compose.material.icons.Icons.Default.Input, // Using Input temporarily as "OK" usually text, we can fix later
            contentDescription = "OK",
            backgroundColor = Color(0xFF1E1E1E), // Black/Dark Grey
            tint = Color.White,
            size = 90.dp, // The inner circle size
            modifier = Modifier.align(Alignment.Center)
        )
        // Note: The icon inside OK is usually text "OK", we will swap icon for text in next phase.
    }
}

@Composable
fun TopSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RemoteButton(Icons.Default.PowerSettingsNew, "Power", tint = Color.Red)
            RemoteButton(Icons.Default.Input, "Input")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RemoteButton(Icons.Default.Person, "Profile", size = 40.dp)
            RemoteButton(Icons.Default.Mic, "Assistant", backgroundColor = Color.White, tint = Color.Black, size = 64.dp)
            RemoteButton(Icons.Default.Settings, "Settings", size = 40.dp)
        }
    }
}

@Composable
fun RemoteButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF2D2D2D),
    tint: Color = Color.White,
    size: Dp = 48.dp
) {
    IconButton(
        onClick = { },
        modifier = modifier
            .size(size)
            .background(backgroundColor, CircleShape),
        colors = IconButtonDefaults.iconButtonColors(contentColor = tint)
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@Preview(showBackground = true)
@Composable
fun RemotePreview() {
    HisenseRemoteTheme {
        RemoteLayout()
    }
}