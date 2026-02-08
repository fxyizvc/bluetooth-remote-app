# 📱 Hisense Tornado Bluetooth Remote

An **open-source Android application** that turns your phone into a **Bluetooth HID Remote Control** for Android TVs.  
Specifically tested on **Hisense Tornado 2.0 / 3.0**, but also works with other Android TV devices.

Unlike traditional Wi-Fi remotes, this app uses the **Bluetooth HID (Human Interface Device) profile**, allowing it to work **offline**, with **zero latency**, and behave exactly like a **physical remote, keyboard, or game controller**.

---

## 🖼️ Preview

<p align="center">
  <img src="https://github.com/user-attachments/assets/c8583065-a1b6-4d58-aad1-d021fefa5ce4" alt="Remote UI" width="260"/>
</p>

---

## 🚀 Features

- ⚡ **Zero-Latency Control**  
  Uses the Bluetooth HID profile for instant response.

- 🔌 **Direct Bluetooth Connection**  
  Connects directly to paired TVs — no Wi-Fi or internet required.

- 🖱️ **Full Navigation Support**  
  D-Pad (Up / Down / Left / Right), OK, Back, Home.

- 🔊 **Media & Power Controls**  
  Volume Up, Volume Down, Mute, Power.

- 📳 **Haptic Feedback**  
  Vibration response on key presses.

- 🎨 **Modern UI**  
  Built entirely using **Kotlin + Jetpack Compose (Material 3)**.

---

## 🛠️ Prerequisites

- **Android Phone:** Android 9.0 (Pie) or higher  
- **Target TV:** Android TV  
  - Hisense Tornado
  - Sony Bravia
  - Nvidia Shield
  - Other Android TV devices
- **Bluetooth:** Enabled on both phone and TV

---

## ⚙️ Setup & Pairing Guide (Important)

Bluetooth HID pairing can be **tricky on modern Android versions** due to battery optimization and visibility limits.  
Follow these steps **exactly** for successful pairing.

### 📲 Phone
1. Open the app.
2. Tap the **blue “Pairing” button** (top-right).
3. Grant permission to make the device **visible to nearby devices** (120 seconds).

### 📺 TV
4. Go to  
   **Settings → Remotes & Accessories → Add Accessory**
5. While the TV is searching:
   - Pull down the notification shade on your phone.
   - **Long-press the Bluetooth icon** to stay on the Bluetooth Settings screen  
     (this prevents the phone from hiding).

6. Select your phone (or **“TV Remote”**) on the TV.
7. Confirm **Pair** on both devices.

---

## 🔧 Button Mapping (One-Time Setup)

Some TV-specific buttons (Netflix, YouTube, Settings, etc.) are sent using **standard F-keys** to guarantee compatibility.

You need to map them once on the TV.

### 📺 TV Setup
1. Install **Button Mapper** from the Play Store.
2. Open **Button Mapper → Add Buttons**.
3. Press the button on the phone app when prompted.
4. Map the keys as shown below:

| Phone App Button | Key Sent | Recommended TV Action |
|------------------|----------|-----------------------|
| Netflix          | F1       | Application → Netflix |
| YouTube          | F2       | Application → YouTube |
| Prime Video      | F3       | Application → Prime Video |
| Media            | F4       | Application → Media Player |
| Settings (⚙️)    | F6       | System → Settings |

---

## 🧩 Tech Stack

- **Language:** Kotlin  
- **UI Framework:** Jetpack Compose (Material 3)  
- **Bluetooth:**  
  - `BluetoothHidDevice`  
  - `BluetoothProfile`
- **Architecture:** MVVM  
- **Design:** Service-based HID controller

---

## 🐛 Troubleshooting

**App crashes on connect**
- Ensure **Nearby Devices** permission is granted.

**TV shows “Connected” but buttons don’t work**
- Unpair the device from both TV and phone.
- Restart the phone.
- Pair again from scratch.

**Phone not visible on TV**
- Keep the **Bluetooth Settings screen open** on your phone while scanning.

---

## 📜 License

This project is **open-source**.  
Feel free to **fork, modify, and improve** it.

Contributions and feedback are welcome! 🚀
