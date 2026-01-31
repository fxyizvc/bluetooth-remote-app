package com.fayizvc.hisenseremote

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.fayizvc.hisenseremote.ui.theme.HisenseRemoteTheme

// --- KEY CODES (Standard Keyboard) ---
object KeyMap {
    const val UP = 82
    const val DOWN = 81
    const val LEFT = 80
    const val RIGHT = 79
    const val ENTER = 40
    const val BACK = 42   // Backspace acts as Back
    const val HOME = 41   // Escape acts as Home/Back often
}

class MainActivity : ComponentActivity() {

    // Make service public so Composables can see it
    var bluetoothService: BluetoothHidService? = null
    private var isBound = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                bluetoothService?.initialize(mainExecutor)
            } else {
                Toast.makeText(this, "Bluetooth permission is required!", Toast.LENGTH_LONG).show()
            }
        }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as BluetoothHidService.LocalBinder
            bluetoothService = binder.getService()
            isBound = true
            Log.d("HisenseRemote", "✅ UI Connected to Service")

            if (hasAllPermissions()) {
                bluetoothService?.initialize(mainExecutor)
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestPermissions()

        setContent {
            HisenseRemoteTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RemoteLayout(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, BluetoothHidService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
            if (!hasPermissions(permissions)) {
                requestPermissionLauncher.launch(permissions)
            } else {
                bluetoothService?.initialize(mainExecutor)
            }
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasAllPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
            return hasPermissions(permissions)
        }
        return true
    }
}

// ==========================================
//    UI CODE
// ==========================================

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
        ControlRow()
        AppButtonsGrid()
    }
}

@Composable
fun NavigationPad() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? MainActivity

    // Function to send key safely
    fun send(code: Int) {
        activity?.bluetoothService?.sendKeyboardCommand(code)
    }

    Box(
        modifier = Modifier.size(220.dp).background(Color(0xFFE0E0E0), CircleShape)
    ) {
        IconButton(onClick = { send(KeyMap.UP) }, modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)) {
            Icon(Icons.Default.KeyboardArrowUp, "Up", tint = Color.Black)
        }
        IconButton(onClick = { send(KeyMap.DOWN) }, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)) {
            Icon(Icons.Default.KeyboardArrowDown, "Down", tint = Color.Black)
        }
        IconButton(onClick = { send(KeyMap.LEFT) }, modifier = Modifier.align(Alignment.CenterStart).padding(start = 10.dp)) {
            Icon(Icons.Default.KeyboardArrowLeft, "Left", tint = Color.Black)
        }
        IconButton(onClick = { send(KeyMap.RIGHT) }, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)) {
            Icon(Icons.Default.KeyboardArrowRight, "Right", tint = Color.Black)
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(90.dp)
                .background(Color(0xFF1E1E1E), CircleShape)
                .clickable { send(KeyMap.ENTER) },
            contentAlignment = Alignment.Center
        ) {
            Text("OK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

@Composable
fun TopSection() {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RemoteButton(Icons.Default.PowerSettingsNew, "Power", tint = Color.Red)

            // PAIRING BUTTON
            IconButton(
                onClick = {
                    val discoverableIntent = Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                        putExtra(android.bluetooth.BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120)
                    }
                    context.startActivity(discoverableIntent)
                    Toast.makeText(context, "Pairing Mode ON!", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.size(48.dp).background(Color(0xFF2D2D2D), CircleShape),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Blue)
            ) {
                Icon(Icons.Default.Input, contentDescription = "Start Pairing")
            }
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
fun ControlRow() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? MainActivity
    fun send(code: Int) = activity?.bluetoothService?.sendKeyboardCommand(code)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mapped BACK to Backspace (42)
        IconButton(
            onClick = { send(KeyMap.BACK) },
            modifier = Modifier.size(40.dp).background(Color(0xFF2D2D2D), CircleShape),
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }

        // Mapped HOME to Escape (41)
        IconButton(
            onClick = { send(KeyMap.HOME) },
            modifier = Modifier.size(48.dp).background(Color.White, CircleShape),
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Black)
        ) { Icon(Icons.Default.Home, "Home") }

        RemoteButton(Icons.Default.Tv, "TV", size = 40.dp)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        VerticalRocker(label = "VOL", iconTop = Icons.Default.Add, iconBottom = Icons.Default.Remove)
        RemoteButton(Icons.Default.VolumeOff, "Mute", size = 40.dp)
        VerticalRocker(label = "CH", iconTop = Icons.Default.KeyboardArrowUp, iconBottom = Icons.Default.KeyboardArrowDown)
    }
}

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

@Composable
fun AppButtonsGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppButton(text = "NETFLIX", color = Color.White, textColor = Color.Red, modifier = Modifier.weight(1f))
            AppButton(text = "YouTube", color = Color.White, textColor = Color.Black, modifier = Modifier.weight(1f))
        }
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
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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