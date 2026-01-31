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
    private var hostDevice: BluetoothDevice? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var executor: Executor? = null

    // --- 1. THE COMPOSITE MAP (Keyboard + Consumer Control) ---
    private val HID_REPORT_DESC = byteArrayOf(
        // --- KEYBOARD (Report ID 1) ---
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
        0xC0.toByte(),                      // End Collection (Keyboard)

        // --- CONSUMER CONTROL (Report ID 2) - For Volume/Power/Home ---
        0x05.toByte(), 0x0C.toByte(),       // Usage Page (Consumer)
        0x09.toByte(), 0x01.toByte(),       // Usage (Consumer Control)
        0xA1.toByte(), 0x01.toByte(),       // Collection (Application)
        0x85.toByte(), 0x02.toByte(),       //   Report ID (2)
        0x15.toByte(), 0x00.toByte(),       //   Logical Min (0)
        0x26.toByte(), 0x3C.toByte(), 0x02.toByte(), //   Logical Max (572)
        0x19.toByte(), 0x00.toByte(),       //   Usage Min (0)
        0x2A.toByte(), 0x3C.toByte(), 0x02.toByte(), //   Usage Max (572)
        0x75.toByte(), 0x10.toByte(),       //   Report Size (16)
        0x95.toByte(), 0x01.toByte(),       //   Report Count (1)
        0x81.toByte(), 0x00.toByte(),       //   Input (Data, Array, Abs)
        0xC0.toByte()                       // End Collection (Consumer)
    )

    private val callback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d("HisenseHID", "🔥🔥🔥 APP STATUS CHANGED: Registered = $registered 🔥🔥🔥")
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            val stateStr = when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
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

        hidDevice?.registerApp(sdpSettings, null, null, executor, callback)
    }

    // --- FUNCTION 1: SEND STANDARD KEYS (Arrows, OK, Back) ---
    @SuppressLint("MissingPermission")
    fun sendKeyboardCommand(keyCode: Int) {
        val target = hostDevice ?: return
        if (hidDevice == null) return

        val pressReport = ByteArray(8)
        pressReport[2] = keyCode.toByte()
        hidDevice?.sendReport(target, 1, pressReport) // Report ID 1

        val releaseReport = ByteArray(8)
        hidDevice?.sendReport(target, 1, releaseReport) // Report ID 1
    }

    // --- FUNCTION 2: SEND MEDIA KEYS (Volume, Mute, Power) ---
    @SuppressLint("MissingPermission")
    fun sendConsumerCommand(usageCode: Int) {
        val target = hostDevice ?: return
        if (hidDevice == null) return

        // Consumer Report Structure: ID(2) + UsageCode(Low Byte) + UsageCode(High Byte)
        val pressReport = ByteArray(2)
        pressReport[0] = (usageCode and 0xFF).toByte()
        pressReport[1] = ((usageCode shr 8) and 0xFF).toByte()

        hidDevice?.sendReport(target, 2, pressReport) // Report ID 2
        Log.d("HisenseHID", "🔊 Sent Media Key: $usageCode")

        // Release
        val releaseReport = ByteArray(2) // 00 00
        hidDevice?.sendReport(target, 2, releaseReport) // Report ID 2
    }

    override fun onBind(intent: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothHidService = this@BluetoothHidService
    }
}