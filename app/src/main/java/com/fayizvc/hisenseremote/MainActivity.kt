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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeOff
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        TopSection()
        NavigationPad()
        ControlRow()     // <-- NEW
        AppButtonsGrid() // <-- NEW
    }
}

// --- NEW SECTION: Volume, Channel, and Center Keys ---
@Composable
fun ControlRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Back Button
        RemoteButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", size = 40.dp)

        // Center: Home Button
        RemoteButton(Icons.Default.Home, "Home", backgroundColor = Color.White, tint = Color.Black, size = 48.dp)

        // Right: TV/Menu Button
        RemoteButton(Icons.Default.Tv, "TV", size = 40.dp)
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Volume Rocker (Vertical Pill)
        VerticalRocker(label = "VOL", iconTop = Icons.Default.Add, iconBottom = Icons.Default.Remove)

        // Mute Button (Small, in the middle)
        RemoteButton(Icons.Default.VolumeOff, "Mute", size = 40.dp)

        // Channel Rocker (Vertical Pill)
        VerticalRocker(label = "CH", iconTop = Icons.Default.KeyboardArrowUp, iconBottom = Icons.Default.KeyboardArrowDown)
    }
}

// --- NEW COMPONENT: Vertical Rocker (For Vol/CH) ---
@Composable
fun VerticalRocker(label: String, iconTop: ImageVector, iconBottom: ImageVector) {
    Column(
        modifier = Modifier
            .width(50.dp)
            .height(110.dp)
            .background(Color(0xFF2D2D2D), RoundedCornerShape(25.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = {}) { Icon(iconTop, "Up", tint = Color.White) }
        Text(label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = {}) { Icon(iconBottom, "Down", tint = Color.White) }
    }
}

// --- NEW SECTION: App Buttons (Netflix, etc) ---
@Composable
fun AppButtonsGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Row 1
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppButton(text = "NETFLIX", color = Color.White, textColor = Color.Red, modifier = Modifier.weight(1f))
            AppButton(text = "YouTube", color = Color.White, textColor = Color.Black, modifier = Modifier.weight(1f))
        }
        // Row 2
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppButton(text = "Prime Video", color = Color.White, textColor = Color(0xFF00A8E1), modifier = Modifier.weight(1f))
            AppButton(text = "MEDIA", color = Color.White, textColor = Color.Black, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun AppButton(text: String, color: Color, textColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(45.dp)
            .background(color, RoundedCornerShape(8.dp))
            .clickable { /* TODO */ },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// --- EXISTING COMPONENTS (TopSection, NavigationPad, RemoteButton) ---
@Composable
fun NavigationPad() {
    Box(
        modifier = Modifier.size(220.dp).background(Color(0xFFE0E0E0), CircleShape)
    ) {
        IconButton(onClick = {}, modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)) {
            Icon(Icons.Default.KeyboardArrowUp, "Up", tint = Color.Black)
        }
        IconButton(onClick = {}, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)) {
            Icon(Icons.Default.KeyboardArrowDown, "Down", tint = Color.Black)
        }
        IconButton(onClick = {}, modifier = Modifier.align(Alignment.CenterStart).padding(start = 10.dp)) {
            Icon(Icons.Default.KeyboardArrowLeft, "Left", tint = Color.Black)
        }
        IconButton(onClick = {}, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)) {
            Icon(Icons.Default.KeyboardArrowRight, "Right", tint = Color.Black)
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(90.dp)
                .background(Color(0xFF1E1E1E), CircleShape)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text("OK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
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
        modifier = modifier.size(size).background(backgroundColor, CircleShape),
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