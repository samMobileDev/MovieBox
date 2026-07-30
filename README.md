<div align="center">

# 🎬 MovieBox — Android Movie Discovery App

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue?style=for-the-badge)](https://developer.android.com/topic/architecture)

*A modern Android application for discovering trending films, searching titles, viewing rich metadata, and managing offline favorites.*



</div>

---

## ✨ Key Features

### 🏠 Home Feed
- **Discover Content:** Browse trending, popular, and top-rated movies.
- **Smooth Navigation:** Horizontally scrollable lists for effortless exploration.

### 🔍 Search & Discovery
- **Real-time Search:** Look up movies by title instantly via remote API requests.
- **State Management:** Robust handling of loading, success, and error states.

### 🎬 Movie Details
- **Rich Metadata:** View overviews, ratings, vote counts, release dates, and popularity metrics.
- **Visuals:** High-resolution poster and backdrop images.

### ❤️ Offline Favorites
- **Local Persistence:** Save favorite movies securely using **Room Database**.
- **Offline Access:** Access your saved favorites anytime without an internet connection.

---

## 🛠 Tech Stack & Libraries

| Category | Technologies |
| :--- | :--- |
| **Language** | Kotlin |
| **UI & Layout** | XML, RecyclerView, ViewBinding, Material Design 3 |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Asynchronous** | Kotlin Coroutines |
| **Networking** | Retrofit, REST API (TMDB) |
| **Local Storage** | Room Database |
| **Navigation** | Fragments, Navigation Component |

---

## 🏗 Architecture & Design Patterns

MovieBox is built following industry-standard Android development principles:
- **MVVM Pattern:** Separation of concerns between UI (Fragments/Activities) and business logic (ViewModels).
- **Asynchronous Programming:** Non-blocking network operations managed efficiently via **Kotlin Coroutines**.
- **Data Layer:** Single source of truth combining remote data from **Retrofit** and local caching via **Room**.
- **UI Safety:** Safe view management using **ViewBinding** to prevent memory leaks and null-pointer exceptions.

---

## 🌐 API Integration

The app integrates with the official **[TMDB API](https://www.themoviedb.org/)** to fetch:
- Trending and Popular movie feeds
- Top-rated movie rankings
- Comprehensive movie details and search results

---

## 📱 Screenshots

<div align="center">
  <img src="movieBox.png" alt="MovieBox App Screenshot" width="700"/>
</div>

---
