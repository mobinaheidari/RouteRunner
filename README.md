# RouteRunner 📍

**RouteRunner** is a modern Android application designed for real-time location tracking and route management. Built entirely with **Jetpack Compose**, it leverages the **MVVM** architecture pattern along with **Clean Architecture** principles to ensure scalability and testability.

## 🚀 Key Features

* **User Authentication:** Secure Login and Registration using local database.
* **Real-time Tracking:** Background Foreground Service for continuous GPS tracking.
* **Interactive Map:** Google Maps integration with polyline route drawing.
* **Data Export:** Export route history to CSV format for external analysis.
* **Offline First:** Full functionality without internet using Room Database.

## 🛠 Tech Stack & Libraries

* **Language:** Kotlin (100%)
* **UI:** Jetpack Compose (Material3)
* **Architecture:** MVVM + MVI (Unidirectional Data Flow)
* **Dependency Injection:** Dagger Hilt
* **Database:** Room (SQLite abstraction)
* **Asynchronicity:** Coroutines & Flow
* **Navigation:** Compose Navigation
* **Maps:** Google Maps Compose & Google Play Services Location

## 🏗 Architecture Overview

This project follows the official Google architecture guidelines:

1.  **UI Layer (Presentation):**
    * **Screens:** Composable functions (`MapScreen`, `LoginScreen`).
    * **ViewModels:** Manage UI state and handle business logic events.
    * **State:** Immutable data classes representing the UI at any point in time.

2.  **Domain Layer:**
    * **Repository Interface:** Defines the contract for data operations.
    * **LocationClient:** Interface for abstracting Android Service logic.

3.  **Data Layer:**
    * **Room Database:** Local persistence source.
    * **DAO:** Data Access Objects for SQL queries.
    * **Implementations:** Concrete classes for Repositories and LocationClient.



## 🤝 Contribution

Contributions are welcome! Please fork the repository and open a pull request.

---
*Developed by Mobina Heidari*
