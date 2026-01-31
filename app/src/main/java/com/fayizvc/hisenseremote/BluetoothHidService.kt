package com.fayizvc.hisenseremote

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.concurrent.Executor

class BluetoothHidService : Service() {

    private val binder = LocalBinder()
    var hidDevice: BluetoothHidDevice? = null

    // FIX 1: Variable to hold the connected TV
    private var hostDevice: BluetoothDevice? = null

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var executor: Executor? = null

    // --- 1. THE MAP (HID Descriptor - Standard Keyboard) ---
    private val HID_REPORT_DESC = byteArrayOf(
        0x05.toByte(), 0x01.toByte(),       // Usage Page (Generic Desktop)
        0x09.toByte(), 0x06.toByte(),       // Usage (Keyboard)
        0xA1.toByte(), 0x01.toByte(),       // Collection (Application)
        0x85.toByte(), 0x01.toByte(),       //   Report ID (1)
        0x05.toByte(), 0x07.toByte(),       //   Usage Page (Key Codes)
        0x19.toByte(), 0xE0.toByte(),       //   Usage Min (224)
        0x29.toByte(), 0xE7.toByte(),       //   Usage Max (231)
        0x15.toByte(), 0x00.toByte(),       //   Logical Min (0)
        0x25.toByte(), 0x01.toByte(),       //   Logical Max (1)
        0x75.toByte(), 0x01.toByte(),       //   Report Size (1)
        0x95.toByte(), 0x08.toByte(),       //   Report Count (8)
        0x81.toByte(), 0x02.toByte(),       //   Input (Data, Var, Abs) - Modifiers
        0x95.toByte(), 0x01.toByte(),       //   Report Count (1)
        0x75.toByte(), 0x08.toByte(),       //   Report Size (8)
        0x81.toByte(), 0x03.toByte(),       //   Input (Cnst, Var, Abs) - Reserved
        0x95.toByte(), 0x05.toByte(),       //   Report Count (5)
        0x75.toByte(), 0x01.toByte(),       //   Report Size (1)
        0x05.toByte(), 0x08.toByte(),       //   Usage Page (LEDs)
        0x19.toByte(), 0x01.toByte(),       //   Usage Min (1)
        0x29.toByte(), 0x05.toByte(),       //   Usage Max (5)
        0x91.toByte(), 0x02.toByte(),       //   Output (Data, Var, Abs) - LEDs
        0x95.toByte(), 0x01.toByte(),       //   Report Count (1)
        0x75.toByte(), 0x03.toByte(),       //   Report Size (3)
        0x91.toByte(), 0x03.toByte(),       //   Output (Cnst, Var, Abs) - Padding
        0x95.toByte(), 0x06.toByte(),       //   Report Count (6)
        0x75.toByte(), 0x08.toByte(),       //   Report Size (8)
        0x15.toByte(), 0x00.toByte(),       //   Logical Min (0)
        0x25.toByte(), 0x65.toByte(),       //   Logical Max (101)
        0x05.toByte(), 0x07.toByte(),       //   Usage Page (Key Codes)
        0x19.toByte(), 0x00.toByte(),       //   Usage Min (0)
        0x29.toByte(), 0x65.toByte(),       //   Usage Max (101)
        0x81.toByte(), 0x00.toByte(),       //   Input (Data, Array) - Key arrays
        0xC0.toByte()                       // End Collection
    )

    private val callback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d("HisenseHID", "🔥🔥🔥 APP STATUS CHANGED: Registered = $registered 🔥🔥🔥")
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            val stateStr = when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    // FIX 2: Save the device when it connects!
                    hostDevice = device
                    "CONNECTED"
                }
                BluetoothProfile.STATE_CONNECTING -> "CONNECTING"
                BluetoothProfile.STATE_DISCONNECTED -> {
                    hostDevice = null
                    "DISCONNECTED"
                }
                else -> "UNKNOWN"
            }
            Log.d("HisenseHID", "🔗 Connection State: $stateStr")
        }
    }

    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = manager?.adapter

        bluetoothAdapter?.getProfileProxy(this, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = proxy as BluetoothHidDevice
                    Log.d("HisenseHID", "✅ HID Proxy Connected!")
                    if (executor != null) initialize(executor!!)
                }
            }
            override fun onServiceDisconnected(profile: Int) {}
        }, BluetoothProfile.HID_DEVICE)
    }

    @SuppressLint("MissingPermission")
    fun initialize(executor: Executor) {
        this.executor = executor
        if (hidDevice == null) return

        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "Hisense Remote",
            "Android Remote",
            "FayizVC",
            0xC0.toByte(),
            HID_REPORT_DESC
        )

        val result = hidDevice?.registerApp(
            sdpSettings,
            null,
            null,
            executor,
            callback
        )

        Log.d("HisenseHID", "🚀 Request Sent. Result: $result")
    }

    // --- SEND KEY FUNCTION ---
    @SuppressLint("MissingPermission")
    fun sendKeyboardCommand(keyCode: Int) {
        // FIX 3: Use the saved 'hostDevice' instead of getDevice(null)
        val target = hostDevice

        if (hidDevice == null || target == null) {
            Log.e("HisenseHID", "❌ Can't send key: Device not connected or Host null.")
            return
        }

        // 1. Press
        val pressReport = ByteArray(8)
        pressReport[2] = keyCode.toByte()
        hidDevice?.sendReport(target, 1, pressReport)
        Log.d("HisenseHID", "📡 Sent Key: $keyCode")

        // 2. Release (Immediately after)
        val releaseReport = ByteArray(8)
        hidDevice?.sendReport(target, 1, releaseReport)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothHidService = this@BluetoothHidService
    }
}