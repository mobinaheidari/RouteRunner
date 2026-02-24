# RouteRunner 📍🏃‍♀️

**RouteRunner** is a modern, highly optimized Android application designed for real-time location tracking and intelligent route management. Built entirely with **Jetpack Compose**, it leverages a strict **Multi-Module Clean Architecture** and the **MVI/MVVM** pattern to ensure massive scalability, separation of concerns, and robust testability.

## 🚀 Key Features

* **User Authentication & Multi-User Support:** Secure local login and registration. Safely handles multiple users on the same device using a `SessionManager` to route background GPS broadcasts to the active user's private database.
* **Advanced Geofencing & Edge Snapping:** Efficiently parses massive GeoJSON boundaries. If a user exits the allowed zone, tracking pauses. Upon re-entry, the app calculates complex vector mathematics to perfectly "snap" the re-entry coordinate to the exact polygon boundary.
* **Discontinuous Route Rendering:** Intelligently splits GPS paths into separate visual segments based on time gaps, ensuring the map accurately reflects where a user walked without drawing straight lines through "forbidden" zones.
* **Real-time Tracking:** Android 14+ compliant Foreground Service for continuous GPS tracking, featuring battery-saving rules (minimum 10-meter displacement updates).
* **Interactive Map:** Google Maps integration with custom polyline route drawing and transparent geofence polygon overlays.
* **Data Export:** Export detailed route history (Latitude, Longitude, Timestamp, Date) to CSV format for external analysis or sharing.
* **Offline First:** Full functionality without an internet connection using a Room SQLite Database.

## 🛠 Tech Stack & Libraries

* **Language:** Kotlin (100%)
* **UI:** Jetpack Compose (Material3)
* **Architecture:** Multi-Module Clean Architecture, MVVM + MVI (Unidirectional Data Flow)
* **Dependency Injection:** Dagger Hilt
* **Database:** Room (SQLite abstraction) & SharedPreferences
* **Asynchronicity:** Kotlin Coroutines & Flow
* **Navigation:** Compose Navigation
* **Maps & Location:** Google Maps Compose, Google Play Services Location, Android Maps Utils (`PolyUtil`)

## 🏗 Architecture Overview

This project heavily enforces the official Google architecture guidelines, separated into distinct, completely decoupled modules:

### 1. The `:app` Module (Business Logic & UI)
* **UI Layer (Presentation):** Composable screens (`MapScreen`, `LoginScreen`) observing strictly immutable UI States via ViewModels.
* **Domain Layer:** Defines repository contracts and business logic.
* **Data Layer:** Houses the `Room` database, DAOs, and the `LocationReceiver`. Uses `SessionManager` to intercept raw broadcasted GPS coordinates and assign them to the currently authenticated user.

### 2. The `:location` Module (Hardware & Sensors)
* **Total Decoupling:** A standalone Android library module that knows *nothing* about users, databases, or app state.
* **Responsibilities:** Manages the FusedLocationProvider, handles foreground service notifications, runs the mathematical geofencing logic, and broadcasts pure, raw `LatLng` data to the system.

## 📸 Screenshots


## 🤝 Contribution

Contributions are welcome! Please fork the repository and open a pull request.

---
*Developed by [Mobina Heidari](https://github.com/mobinaheidari)*
