# SolarRadar — Technician Mobile App

> **Part 2 of the SolarRadar project.** The web admin panel (Part 1) is used by administrators to manage solar installation sites, assign jobs to technicians, and review submitted reports. This Android app is the mobile companion used by field technicians to view their assigned jobs, start work, and submit reports with photos — all in sync with the same Firebase backend.

---

## Overview

SolarRadar is a field service management system for solar panel installation and maintenance companies. This app gives technicians everything they need on-site:

- See jobs assigned to them with deadlines and priority indicators
- Start a job and change its status in real time
- Submit work reports with photos directly from the field
- Edit previously submitted reports
- View the full job detail including site address (opens in Google Maps) and attachments uploaded by the admin
- Switch between English and Lithuanian
- Toggle dark / light mode

---

## Screenshots

> _Add screenshots here_

| Login | Dashboard | Jobs | Job Detail |
|-------|-----------|------|------------|
| | | | |

| Report Form | Profile | Settings |
|-------------|---------|----------|
| | | |

---

## Features

### Authentication
- Firebase Email/Password login
- Role check on sign-in — only accounts with `role: "technician"` in Firestore can access the app
- Session persists across app restarts

### Dashboard
- Live counts of Open, In Progress, and Due This Week jobs
- Urgent deadlines list with color-coded indicators (red → orange → blue as deadline approaches)

### Jobs
- **Current** tab: open and in-progress jobs with pull-to-refresh
- **Resolved** tab: completed jobs
- Tap a job to see full details

### Job Detail
- Site name, job type, assigned technician, deadline with days remaining
- Site address with "Open in Maps" (Google Maps)
- Admin-uploaded attachments (photos from web panel)
- Required expertise chips
- **Start Job** button (open → in progress)
- **Submit Report** button (in progress)
- **Report section** with Edit button for resolved jobs

### Report Form & Edit Report
- Work status dropdown (Completed / Not Completed / Requires Maintenance)
- Notes field
- Photo picker — upload multiple photos to Firebase Storage
- Editing a report sets an `editedByTechnicianName` flag visible to the web admin panel

### Profile
- Displays technician name, email, member ID, and expertise areas
- Avatar circle with initials

### Settings
- **Dark Mode** toggle — saved to device storage, persists across restarts
- **Language** selector — English / Lithuanian

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | Single Activity, ViewModel per screen |
| Navigation | Jetpack Navigation Compose |
| Auth | Firebase Authentication |
| Database | Cloud Firestore |
| Storage | Firebase Storage (photo uploads) |
| Image loading | Coil |
| Build system | Gradle 9.3.1 with Kotlin DSL |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 36 |

---

## Project Structure

```
app/src/main/java/com/example/solarradarapp/
├── MainActivity.kt
├── model/              # Data classes: Job, Report, TechnicianProfile, Enums
├── navigation/         # AppNavigation.kt — single NavHost with all routes
├── ui/
│   ├── components/     # Reusable composables: StatusChip, TopBarAvatar
│   ├── dashboard/      # Dashboard screen + ViewModel
│   ├── home/           # Profile screen + ViewModel
│   ├── jobs/           # JobList, JobDetail screens + ViewModels
│   ├── login/          # Login screen + ViewModel
│   ├── main/           # MainScreen (bottom nav scaffold)
│   ├── report/         # ReportForm, EditReport screens + ViewModels
│   ├── settings/       # Settings screen (dark mode + language)
│   ├── strings/        # AppStrings data class, EN/LT translations
│   └── theme/          # AppColors (light/dark), Color, Theme, Type
└── util/
    ├── LanguageManager.kt  # Language preference (SharedPreferences + StateFlow)
    └── ThemeManager.kt     # Dark mode preference (SharedPreferences + StateFlow)
```

---

## Firebase Setup

This project requires a Firebase project connected to the same Firestore database as the web admin panel.

1. Go to [Firebase Console](https://console.firebase.google.com) and open your project
2. Add an Android app with package name `com.example.solarradarapp`
3. Download `google-services.json` and place it in the `app/` directory
4. Enable **Email/Password** authentication in Firebase Auth
5. Make sure Firestore and Storage rules allow authenticated technician reads/writes

### Firestore Structure (key collections)

```
users/{uid}
  displayName, email, memberId, role ("technician"), expertise []

jobs/{jobId}
  jobId, siteName, type, status, assignedTo, assignedName,
  deadline, description, requiredExpertise [], siteRef

reports/{reportId}
  jobId, status, notes, photoUrls [], submittedAt,
  submittedByName, editFlag, editedByTechnicianName

sites/{siteId}
  name, address
```

---

## Build & Run

```bash
# Clone the repo
git clone <repo-url>
cd SolarRadarApp

# Add your google-services.json to app/

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Install on connected device
./gradlew installDebug
```

Or open the project in **Android Studio** (Ladybug or newer) and click **Run**.

---

## Related Project

The **web admin panel** (Part 1 of SolarRadar) is built for administrators and managers. It provides a full dashboard to:
- Manage technician accounts and their expertise
- Create and assign jobs to technicians
- Upload job attachments for field reference
- Review submitted and edited reports
- Monitor job statuses and deadlines in real time

Both the web app and this mobile app share the same Firebase project and Firestore database, keeping all data in sync.

---

## License

This project is private. All rights reserved.
