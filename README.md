<div align="center">

# 🔔 XCR School Bell

### Enterprise School Bell Management System

[![Java](https://img.shields.io/badge/Java-11%2B-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-blue?style=for-the-badge)](https://github.com/X-croot/SchoolBell)
[![Release](https://img.shields.io/badge/Version-1.0.0-purple?style=for-the-badge)](https://github.com/X-croot/SchoolBell/releases)

**Advanced, easy-to-use, and fully customizable bell system for schools**

[📥 Download Now](#-installation) • [📖 User Guide](#-user-guide) • [🛠️ Features](#-features) • [❓ FAQ](#-frequently-asked-questions)

---

</div>



## 📋 Table of Contents

- [🎯 About The Project](#-about-the-project)
- [✨ Features](#-features)
- [📦 Requirements](#-requirements)
- [📥 Installation](#-installation)
  - [Windows Installation](#-windows-installation)
  - [Linux Installation](#-linux-installation)
  - [macOS Installation](#-macos-installation)
  - [Building From Source](#-building-from-source)
- [🚀 User Guide](#-user-guide)
- [📸 Screenshots](#-screenshots)
- [⚙️ Configuration](#️-configuration)
- [❓ Frequently Asked Questions](#-frequently-asked-questions)
- [🐛 Troubleshooting](#-troubleshooting)
- [📄 License](#-license)

---

## 🎯 About The Project

**XCR School Bell** is a professional bell management system designed for schools. With its modern and user-friendly interface, you can easily manage different bell types such as student bell, teacher bell, and break bell.

### Why XCR School Bell?

- 🎨 **Modern Interface**: Elegant design with dark theme that's easy on the eyes
- ⏰ **Automatic Bells**: Automatic bell ringing at scheduled times
- 🎵 **Multi-Format Support**: MP3, WAV, OGG, FLAC, M4A, AAC and more
- 🎤 **Live Announcements**: Make instant announcements via microphone
- 💾 **Backup System**: Safely store your settings
- 🎹 **Soundpad**: Quick access panel for custom sounds

---

## ✨ Features

### 📅 Schedule Management
| Feature | Description |
|---------|-------------|
| 📆 7-Day Schedule | Plan your entire week from Monday to Sunday |
| ➕ Add/Remove Lessons | Add unlimited lesson times |
| 📋 Copy/Paste | Easily copy one day to another |
| 📑 Copy to All | Apply one schedule to all days |

### 🔔 Bell Types
| Bell Type | Icon | Description |
|-----------|------|-------------|
| Student Bell | 🎓 | Signals the start of class |
| Teacher Bell | 👨‍🏫 | Special alert bell for teachers |
| Break Bell | ☕ | Signals end of class/break time |

### 🎹 Soundpad Features
- ▶️ One-click sound playback
- ⏸️ Pause and resume functionality
- 🎚️ Set sound range (start/end points)
- 🎨 Color-coded sounds
- ♾️ Unlimited sound additions

### 🎤 Live Announcement System
- 🔴 Press and hold to announce
- 🎚️ Microphone volume adjustment
- 📢 Instant speaker output

### 💾 Backup and Restore
- 📅 Dated automatic backups
- 📤 Export functionality
- 📥 Import functionality
- 🔄 Easy restoration

---

## 📦 Requirements

### Minimum System Requirements

| Component | Requirement |
|-----------|-------------|
| **Operating System** | Windows 10/11, Ubuntu 18.04+, macOS 10.14+ |
| **Java** | JDK 11 or higher |
| **RAM** | Minimum 512 MB |
| **Disk Space** | Minimum 100 MB |
| **Sound Card** | Any audio output device |

### Optional (Recommended)
| Component | Description |
|-----------|-------------|
| **FFmpeg** | For advanced audio format support |
| **Microphone** | For live announcement feature |

---

## 📥 Installation

### 🪟 Windows Installation

#### Method 1: Pre-built JAR File (Recommended)

1. **Install Java JDK 11+**
   
   If Java is not installed, download from [Adoptium](https://adoptium.net/):
   ```
   https://adoptium.net/temurin/releases/
   ```
   
   Or using winget:
   ```powershell
   winget install EclipseAdoptium.Temurin.11.JDK
   ```

2. **Download the Application**
   
   Download `XCR_School_Bell.jar` from the [Releases](https://github.com/X-croot/SchoolBell/releases) page.

3. **Run the Application**
   
   Double-click the JAR file or run from command line:
   ```cmd
   java -jar XCR_School_Bell.jar
   ```

4. **(Optional) Install FFmpeg**
   
   For advanced audio format support:
   ```powershell
   # Using Chocolatey
   choco install ffmpeg
   
   # Or using Winget
   winget install FFmpeg.FFmpeg
   ```

#### Method 2: Create Desktop Shortcut

1. Right-click on Desktop → **New** → **Shortcut**
2. Enter the location:
   ```
   javaw -jar "C:\Program Files\SchoolBell\XCR_School_Bell.jar"
   ```
3. Name it: `School Bell`
4. Choose your preferred icon

#### Method 3: Add to Windows Startup

1. Press `Win + R`
2. Type `shell:startup` and press Enter
3. Copy the JAR file shortcut to the opened folder

---

### 🐧 Linux Installation

#### Ubuntu/Debian Based Distributions

1. **Install Java JDK 11+**
   ```bash
   # Update package list
   sudo apt update
   
   # Install OpenJDK 11
   sudo apt install openjdk-11-jdk -y
   
   # Verify Java version
   java -version
   ```

2. **Install FFmpeg (Optional but Recommended)**
   ```bash
   sudo apt install ffmpeg -y
   ```

3. **Download the Application**
   ```bash
   # Create download directory
   mkdir -p ~/SchoolBell
   cd ~/SchoolBell
   
   # Download JAR file
   wget https://github.com/X-croot/SchoolBell/releases/download/1.0.0/XCR_School_Bell.jar
   ```

4. **Run the Application**
   ```bash
   java -jar XCR_School_Bell.jar
   ```

5. **(Optional) Create Desktop Shortcut**
   ```bash
   cat > ~/.local/share/applications/schoolbell.desktop << EOF
   [Desktop Entry]
   Name=School Bell
   Comment=School Bell Management System
   Exec=java -jar $HOME/SchoolBell/XCR_School_Bell.jar
   Icon=audio-volume-high
   Terminal=false
   Type=Application
   Categories=Education;Audio;
   EOF
   ```

#### Fedora/RHEL Based Distributions

```bash
# Install Java
sudo dnf install java-11-openjdk -y

# Install FFmpeg
sudo dnf install ffmpeg -y

# Run the application
java -jar XCR_School_Bell.jar
```

#### Arch Linux

```bash
# Install Java
sudo pacman -S jdk11-openjdk

# Install FFmpeg
sudo pacman -S ffmpeg

# Run the application
java -jar XCR_School_Bell.jar
```

#### Running as System Service (Server/Kiosk Mode)

```bash
# Create service file
sudo tee /etc/systemd/system/schoolbell.service << EOF
[Unit]
Description=School Bell Application
After=network.target sound.target

[Service]
Type=simple
User=$USER
Environment=DISPLAY=:0
ExecStart=/usr/bin/java -jar /home/$USER/SchoolBell/XCR_School_Bell.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# Enable and start the service
sudo systemctl daemon-reload
sudo systemctl enable schoolbell
sudo systemctl start schoolbell

# Check service status
sudo systemctl status schoolbell
```

---

### 🍎 macOS Installation

1. **Install Java JDK 11+**
   ```bash
   # Using Homebrew
   brew install openjdk@11
   
   # Add to PATH
   echo 'export PATH="/usr/local/opt/openjdk@11/bin:$PATH"' >> ~/.zshrc
   source ~/.zshrc
   ```

2. **Install FFmpeg**
   ```bash
   brew install ffmpeg
   ```

3. **Run the Application**
   ```bash
   java -jar XCR_School_Bell.jar
   ```

---

### 🔧 Building From Source

If you want to build the project yourself:

1. **Install Requirements**
   - JDK 11+
   - Maven 3.6+
   - Git

2. **Clone the Repository**
   ```bash
   git clone https://github.com/X-croot/SchoolBell.git
   cd SchoolBell
   ```

3. **Build with Maven**
   ```bash
   # Download dependencies and compile
   mvn clean package
   
   # Create executable JAR
   mvn package shade:shade
   ```

4. **Run the Built JAR**
   ```bash
   java -jar target/XCR_School_Bell.jar
   ```

---

## 🚀 User Guide

### 🏁 Getting Started

1. Launch the application
2. Make sure **"BELL SYSTEM ACTIVE"** is enabled at the top
3. Click on the **"📅 Schedule"** tab from the menu

### 📅 Creating a Schedule

```
1. Open the Schedule tab
2. Select the desired day (Monday, Tuesday, etc.)
3. Click the "➕ Add Lesson" button
4. For each lesson, enter:
   - Student Bell Time (e.g., 08:30)
   - Teacher Bell Time (e.g., 08:25)
   - Break Bell Time (e.g., 09:10)
5. Add as many lessons as needed
6. Click the "💾 SAVE" button
```

### 🔊 Setting Bell Sounds

```
1. Open the "🔊 Bell Sounds" tab
2. Find the relevant bell type (Student/Teacher/Break)
3. Click the "📂 Browse" button
4. Select your sound file (MP3, WAV, OGG, etc.)
5. Test with the "▶️ Test" button
6. Adjust the volume level
```

### 🎹 Using Soundpad

```
1. Open the "🎹 Soundpad" tab
2. Click the "➕ ADD NEW SOUND" button
3. Select a sound file
4. Give it a name
5. Click on the added sound card to play
6. Click again to stop
```

### 🎤 Making Live Announcements

```
1. Open the "🎤 Live Announcement" tab
2. Press and hold the microphone icon
3. Speak your announcement
4. Release to stop
```

### 💾 Backup Operations

```
Creating a Backup:
1. Open the "💾 Backup" tab
2. Click the "💾 Create Backup" button

Restoring from Backup:
1. Select a backup from the list
2. Click the "🔄 Restore" button
3. Confirm the action
```

---

## 📸 Screenshots

> 📌 **Note**: Visit the [Wiki](https://github.com/X-croot/SchoolBell/wiki) page for screenshots or download the application to experience it yourself!

### Interface Features:
- 🌙 Dark theme (eye-friendly)
- 🎨 Colorful accent colors
- 📱 Responsive design
- 🖱️ Intuitive usage

---

## ⚙️ Configuration

### Folder Structure

Application data is stored in the following folders:

```
data/
├── config.json          # Main configuration file
├── sounds/              # Bell and soundpad audio files
│   ├── student.wav
│   ├── teacher.wav
│   ├── break.wav
│   └── snd_*.wav        # Soundpad sounds
└── backups/             # Backup files
    └── backup_*.json
```

### config.json Structure

```json
{
  "bellActive": true,
  "micVol": 100,
  "schedule": [
    {
      "day": "Monday",
      "lessons": [
        {
          "student": "08:30",
          "teacher": "08:25",
          "break": "09:10"
        }
      ]
    }
  ],
  "sounds": {
    "student": {"file": "student", "volume": 100},
    "teacher": {"file": "teacher", "volume": 100},
    "break": {"file": "break", "volume": 100}
  },
  "soundpad": []
}
```

---

## ❓ Frequently Asked Questions

<details>
<summary><b>🔹 How do I check if Java is installed?</b></summary>

Type the following in command line:
```bash
java -version
```
If you see version information, Java is installed.
</details>

<details>
<summary><b>🔹 The application won't open, what should I do?</b></summary>

1. Make sure Java is installed
2. Try running from command line:
   ```bash
   java -jar XCR_School_Bell.jar
   ```
3. Check the error message
</details>

<details>
<summary><b>🔹 Sound is not playing, how do I fix it?</b></summary>

1. Check system volume
2. Make sure the sound file is in the correct format
3. Install FFmpeg if not installed
4. Check in-app volume level
</details>

<details>
<summary><b>🔹 MP3 files won't play?</b></summary>

Install FFmpeg. Without FFmpeg, only WAV files can be played.
</details>

<details>
<summary><b>🔹 My settings are lost?</b></summary>

1. Check the `data/backups/` folder
2. Restore the latest backup
3. Or manually edit `data/config.json`
</details>

<details>
<summary><b>🔹 Can the application start automatically at system startup?</b></summary>

**Windows**: Add JAR shortcut to `shell:startup` folder

**Linux**: Create a systemd service (see instructions above)
</details>

<details>
<summary><b>🔹 Can I use it on multiple computers?</b></summary>

Yes! You can copy the `data/` folder or backup files to another computer.
</details>

---

## 🐛 Troubleshooting

### ❌ "Java not found" Error

```bash
# Windows
set PATH=%PATH%;C:\Program Files\Java\jdk-11\bin

# Linux/macOS
export PATH=$PATH:/usr/lib/jvm/java-11-openjdk/bin
```

### ❌ Sound Output Issues

1. Check default audio output device
2. Restart PulseAudio service (Linux):
   ```bash
   pulseaudio -k && pulseaudio --start
   ```

### ❌ High CPU Usage

- Stop sounds playing in the background
- Remove unnecessary sounds from Soundpad

### ❌ Application Freezing

1. Close the application
2. Delete `data/config.json`
3. Restart the application (clean installation)

### ❌ Sound Permission Error on Linux

```bash
# Add user to audio group
sudo usermod -a -G audio $USER

# Log out and log back in, or:
newgrp audio
```

---

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2026 Can Ünüvar

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---
