# Torch App 🔦

A feature-rich Android flashlight application with advanced controls and customizable settings.

## Features

### 🔦 Core Functionality
- **Instant Torch Control** - Quick on/off toggle with large, accessible button
- **SOS Mode** - Emergency SOS signal with Morse code pattern (···---···)
- **Strobe Mode** - Adjustable flashing patterns for various scenarios
- **Auto-Blink** - Customizable automatic blinking intervals

### ⚙️ Advanced Controls
- **Rate Control** - Adjustable flash speed with real-time preview
- **Sound Effects** - Optional audio feedback for button interactions
- **Vibration Feedback** - Haptic response for enhanced user experience
- **Settings Menu** - Comprehensive customization options

### 🎨 User Interface
- **Clean Material Design** - Modern, intuitive interface
- **Dark Theme** - Optimized for low-light conditions
- **Portrait Lock** - Prevents accidental screen rotation
- **Accessibility** - Large buttons and clear visual indicators

## Technical Specifications

- **Platform**: Android (API 21+)
- **Language**: Java
- **Architecture**: Native Android with Camera2 API
- **Permissions**: Camera, Flashlight, Wake Lock
- **Dependencies**: AndroidX, Material Components

## Requirements

- Android 5.0 (API level 21) or higher
- Device with camera flash/LED
- Camera permission for flashlight access


### Prerequisites
- Android Studio Arctic Fox or later
- Gradle 9.2.0 or compatible version
- Android SDK with API 21+ support

## Usage

### Basic Operation
1. **Turn On/Off**: Tap the main torch button
2. **SOS Mode**: Press and hold the SOS button for emergency signals
3. **Settings**: Use the menu to access configuration options

### Advanced Features
- **Rate Control**: Adjust flash speed using the slider in settings
- **Auto-Blink**: Enable automatic blinking with customizable intervals
- **Sound/Vibration**: Toggle feedback options in settings menu


### Build Configuration
- **Target SDK**: 34 (Android 14)
- **Minimum SDK**: 21 (Android 5.0)
- **Compile SDK**: 34
- **Build Tools**: Android Gradle Plugin 8.3.2

### Key Components
- **Camera2 API**: Direct hardware flash control
- **ViewBinding**: Type-safe view references
- **SharedPreferences**: User settings persistence
- **Material Design Components**: Modern UI elements


### Version 2.0
- ✨ Added SOS mode with Morse code pattern
- ✨ Implemented rate control for flash speed
- ✨ Added auto-blink functionality
- ✨ Enhanced UI with Material Design
- ✨ Added sound and vibration feedback
- 🔧 Updated to modern Android APIs
- 🔧 Improved accessibility features

### Version 1.0
- 🎉 Initial release
- ⚡ Basic torch on/off functionality
- ⚙️ Simple settings interface
