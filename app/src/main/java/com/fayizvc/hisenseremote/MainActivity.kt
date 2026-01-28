package com.fayizvc.hisenseremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp) // Space between sections
    ) {
        // Section 1: Top Power/Input
        Text(text = "Header Section (Power/Input)", color = Color.White)

        // Section 2: Navigation (The Circle)
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color.Gray)
        ) {
            Text("Navigation Pad", modifier = Modifier.align(Alignment.Center))
        }

        // Section 3: Vol/Ch
        Text(text = "Volume / Channel / Home", color = Color.White)

        // Section 4: App Buttons
        Text(text = "Netflix / YouTube / Media", color = Color.White)
    }
}

@Preview(showBackground = true)
@Composable
fun RemotePreview() {
    HisenseRemoteTheme {
        RemoteLayout()
    }
}