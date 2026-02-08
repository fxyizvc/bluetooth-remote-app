package com.fayizvc.hisenseremote

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.fayizvc.hisenseremote.ui.theme.HisenseRemoteTheme

object KeyMap {
    const val UP = 82; const val DOWN = 81; const val LEFT = 80; const val RIGHT = 79
    const val ENTER = 40
    // Changed BACK to 41 (Escape) - works universally as Back on Android TV
    const val BACK = 41
    const val NETFLIX = 58; const val YOUTUBE = 59; const val PRIME = 60; const val MEDIA = 61
    const val VOL_UP = 233; const val VOL_DOWN = 234; const val MUTE = 226
    const val POWER = 48
    // Changed HOME to 547 (0x223 AC Home) - standard Home command
    const val HOME = 547
    // FIX: Using F5 (62) for Settings - requires Button Mapper on TV if native fails
    const val SETTINGS = 62
}

class MainActivity : ComponentActivity() {
    var bluetoothService: BluetoothHidService? = null
    private var isBound = false

    // FIX: Safer vibration function that won't crash if permission is missing
    private fun vibrate() {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(50)
            }
        } catch (e: Exception) {
            Log.e("HisenseRemote", "Vibration failed: ${e.message}")
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.values.all { granted -> granted }) bluetoothService?.initialize(mainExecutor)
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            bluetoothService = (service as BluetoothHidService.LocalBinder).getService()
            isBound = true
            if (hasAllPermissions()) bluetoothService?.initialize(mainExecutor)
        }
        override fun onServiceDisconnected(arg0: ComponentName) { isBound = false }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestPermissions()
        setContent { HisenseRemoteTheme { Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding -> RemoteLayout(Modifier.padding(innerPadding), ::vibrate) } } }
    }

    override fun onStart() { super.onStart(); Intent(this, BluetoothHidService::class.java).also { bindService(it, connection, Context.BIND_AUTO_CREATE) } }
    override fun onStop() { super.onStop(); if (isBound) { unbindService(connection); isBound = false } }
    private fun checkAndRequestPermissions() { if (Build.VERSION.SDK_INT >= 31 && !hasAllPermissions()) requestPermissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE)) else bluetoothService?.initialize(mainExecutor) }
    private fun hasAllPermissions() = if (Build.VERSION.SDK_INT >= 31) arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE).all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED } else true

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        val manager = getSystemService(BluetoothManager::class.java)
        return manager?.adapter?.bondedDevices?.toList() ?: emptyList()
    }
}

@Composable
fun RemoteLayout(modifier: Modifier = Modifier, onVibrate: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFF1E1E1E)).verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        TopSection(onVibrate)
        NavigationPad(onVibrate)
        ControlRow(onVibrate)
        AppButtonsGrid(onVibrate)
    }
}

@SuppressLint("MissingPermission")
@Composable
fun TopSection(onVibrate: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? MainActivity
    var showDeviceList by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = { onVibrate(); activity?.bluetoothService?.sendConsumerCommand(KeyMap.POWER) }, modifier = Modifier.size(48.dp).background(Color(0xFF2D2D2D), CircleShape)) {
                Icon(Icons.Default.PowerSettingsNew, "Power", tint = Color.Red)
            }

            // Connect Button
            Box {
                IconButton(onClick = { showDeviceList = true }, modifier = Modifier.size(48.dp).background(Color(0xFF2D2D2D), CircleShape)) {
                    Icon(Icons.Default.Link, "Connect", tint = Color.Green)
                }
                DropdownMenu(expanded = showDeviceList, onDismissRequest = { showDeviceList = false }) {
                    val devices = activity?.getPairedDevices() ?: emptyList()
                    if (devices.isEmpty()) {
                        DropdownMenuItem(text = { Text("No Paired Devices") }, onClick = { showDeviceList = false })
                    } else {
                        devices.forEach { device ->
                            DropdownMenuItem(
                                text = { Text(device.name ?: "Unknown Device") },
                                onClick = {
                                    onVibrate() // This won't crash now
                                    activity?.bluetoothService?.connectToDevice(device)
                                    showDeviceList = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            RemoteButton(Icons.Default.Person, "Profile", size = 40.dp, onClick = onVibrate)
            RemoteButton(Icons.Default.Mic, "Assistant", backgroundColor = Color.White, tint = Color.Black, size = 64.dp, onClick = onVibrate)
            // WIRED SETTINGS BUTTON
            RemoteButton(Icons.Default.Settings, "Settings", size = 40.dp, onClick = {
                onVibrate()
                // FIX: Send Keyboard Command (F5 / 62) for Settings
                activity?.bluetoothService?.sendKeyboardCommand(KeyMap.SETTINGS)
            })
        }
    }
}

@Composable
fun NavigationPad(onVibrate: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current; val activity = context as? MainActivity
    fun send(code: Int) { onVibrate(); activity?.bluetoothService?.sendKeyboardCommand(code) }
    Box(modifier = Modifier.size(220.dp).background(Color(0xFFE0E0E0), CircleShape)) {
        IconButton(onClick = { send(KeyMap.UP) }, modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)) { Icon(Icons.Default.KeyboardArrowUp, "Up", tint = Color.Black) }
        IconButton(onClick = { send(KeyMap.DOWN) }, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)) { Icon(Icons.Default.KeyboardArrowDown, "Down", tint = Color.Black) }
        IconButton(onClick = { send(KeyMap.LEFT) }, modifier = Modifier.align(Alignment.CenterStart).padding(start = 10.dp)) { Icon(Icons.Default.KeyboardArrowLeft, "Left", tint = Color.Black) }
        IconButton(onClick = { send(KeyMap.RIGHT) }, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)) { Icon(Icons.Default.KeyboardArrowRight, "Right", tint = Color.Black) }
        Box(modifier = Modifier.align(Alignment.Center).size(90.dp).background(Color(0xFF1E1E1E), CircleShape).clickable { send(KeyMap.ENTER) }, contentAlignment = Alignment.Center) { Text("OK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
    }
}

@Composable
fun ControlRow(onVibrate: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current; val activity = context as? MainActivity
    fun sendKey(code: Int) { onVibrate(); activity?.bluetoothService?.sendKeyboardCommand(code) }
    fun sendMedia(code: Int) { onVibrate(); activity?.bluetoothService?.sendConsumerCommand(code) }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        // Updated Back Button to use sendKey (Keyboard Back/Escape)
        IconButton(onClick = { sendKey(KeyMap.BACK) }, modifier = Modifier.size(40.dp).background(Color(0xFF2D2D2D), CircleShape)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
        // Updated Home Button to use sendMedia (Consumer Home)
        IconButton(onClick = { sendMedia(KeyMap.HOME) }, modifier = Modifier.size(48.dp).background(Color.White, CircleShape)) { Icon(Icons.Default.Home, "Home", tint = Color.Black) }
        RemoteButton(Icons.Default.Tv, "TV", size = 40.dp, onClick = onVibrate)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Column(modifier = Modifier.width(50.dp).height(110.dp).background(Color(0xFF2D2D2D), RoundedCornerShape(25.dp)), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = { sendMedia(KeyMap.VOL_UP) }) { Icon(Icons.Default.Add, "Vol+", tint = Color.White) }
            Text("VOL", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { sendMedia(KeyMap.VOL_DOWN) }) { Icon(Icons.Default.Remove, "Vol-", tint = Color.White) }
        }
        IconButton(onClick = { sendMedia(KeyMap.MUTE) }, modifier = Modifier.size(40.dp).background(Color(0xFF2D2D2D), CircleShape)) { Icon(Icons.Default.VolumeOff, "Mute", tint = Color.White) }
        Column(modifier = Modifier.width(50.dp).height(110.dp).background(Color(0xFF2D2D2D), RoundedCornerShape(25.dp)), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = { sendKey(KeyMap.UP) }) { Icon(Icons.Default.KeyboardArrowUp, "Ch+", tint = Color.White) }
            Text("CH", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { sendKey(KeyMap.DOWN) }) { Icon(Icons.Default.KeyboardArrowDown, "Ch-", tint = Color.White) }
        }
    }
}

@Composable
fun AppButtonsGrid(onVibrate: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current; val activity = context as? MainActivity
    fun sendKey(code: Int) { onVibrate(); activity?.bluetoothService?.sendKeyboardCommand(code) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppButton("NETFLIX", Color.White, Color.Red, Modifier.weight(1f)) { sendKey(KeyMap.NETFLIX) }
            AppButton("YouTube", Color.White, Color.Black, Modifier.weight(1f)) { sendKey(KeyMap.YOUTUBE) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppButton("Prime Video", Color.White, Color(0xFF00A8E1), Modifier.weight(1f)) { sendKey(KeyMap.PRIME) }
            AppButton("MEDIA", Color.White, Color.Black, Modifier.weight(1f)) { sendKey(KeyMap.MEDIA) }
        }
    }
}

@Composable
fun AppButton(text: String, color: Color, textColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.height(45.dp).background(color, RoundedCornerShape(8.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) { Text(text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
}

@Composable
fun RemoteButton(icon: ImageVector, contentDescription: String, modifier: Modifier = Modifier, backgroundColor: Color = Color(0xFF2D2D2D), tint: Color = Color.White, size: Dp = 48.dp, onClick: () -> Unit) {
    IconButton(onClick = { onClick() }, modifier = modifier.size(size).background(backgroundColor, CircleShape)) { Icon(imageVector = icon, contentDescription = contentDescription, tint = tint) }
}

@Preview(showBackground = true) @Composable fun RemotePreview() { HisenseRemoteTheme { RemoteLayout(onVibrate = {}) } }