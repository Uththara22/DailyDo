# DailyDo - Habit, Mood & Hydration Tracker

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Min%20SDK-24%2B-0B57D0?style=for-the-badge&logo=android&logoColor=white" alt="Minimum SDK 24+" />
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin 2.0.21" />
  <img src="https://img.shields.io/badge/Material-1.12.0-009688?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material Components" />
</p>

<p align="center">
  <strong>Build better days with simple daily steps.</strong><br />
  DailyDo is an Android lifestyle app for tracking habits, logging moods, managing hydration, and reviewing daily progress.
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#-screenshots">Screenshots</a> •
  <a href="#-tech-stack">Tech Stack</a>
</p>

---

## ✨ Overview

DailyDo helps users stay consistent with healthy routines through a focused mobile experience. The app combines habit tracking, mood journaling, hydration reminders, daily summaries, notifications, and a home screen widget into one clean Android workflow.

## 🚀 Features

| Icon | Feature | Description |
| --- | --- | --- |
| ✅ | Habit Tracker | Create habits, track daily completion, monitor progress, and keep streak motivation visible. |
| 😊 | Mood Journal | Log mood entries with emoji-style selectors, optional notes, history, and mood trend support. |
| 💧 | Hydration Manager | Set a daily water goal, add intake amounts, view progress, and configure reminders. |
| 📊 | Progress Summary | Review habit, mood, and hydration averages by date through a dedicated summary screen. |
| 🔔 | Smart Reminders | Uses WorkManager, alarm receivers, boot handling, and notifications for reminder support. |
| 🧩 | Home Widget | Quick habit progress visibility directly from the Android home screen. |
| 🔐 | User Flow | Includes onboarding, welcome, sign up, sign in, settings, about, and logout flows. |
| 💾 | Local Storage | SharedPreferences keeps core app data available offline on the device. |

## 📱 Screenshots

### Launch

<p align="center">
  <img src="assets/pdf-screenshots/page3_1_Image48.png" alt="DailyDo launch screen" width="95" />
</p>

### Onboarding & Welcome

<p align="center">
  <img src="assets/pdf-screenshots/page4_1_Image51.jpg" alt="Onboarding screen 1" width="90" />
  <img src="assets/pdf-screenshots/page4_2_Image52.jpg" alt="Onboarding screen 2" width="90" />
  <img src="assets/pdf-screenshots/page4_3_Image53.jpg" alt="Onboarding screen 3" width="90" />
  <img src="assets/pdf-screenshots/page4_4_Image54.jpg" alt="Welcome screen" width="90" />
</p>

### Authentication & Habits

<p align="center">
  <img src="assets/pdf-screenshots/page5_1_Image57.png" alt="Signup screen" width="90" />
  <img src="assets/pdf-screenshots/page5_2_Image58.png" alt="Sign in screen" width="90" />
  <img src="assets/pdf-screenshots/page5_3_Image59.png" alt="Habit tracker screen" width="90" />
  <img src="assets/pdf-screenshots/page5_4_Image60.png" alt="Add habit dialog" width="90" />
</p>

### Mood & Hydration

<p align="center">
  <img src="assets/pdf-screenshots/page6_1_Image63.png" alt="Mood journal screen" width="90" />
  <img src="assets/pdf-screenshots/page6_2_Image64.png" alt="Recent mood entries" width="90" />
  <img src="assets/pdf-screenshots/page6_3_Image65.png" alt="Hydration tracker screen" width="90" />
  <img src="assets/pdf-screenshots/page6_4_Image66.png" alt="Set hydration goal dialog" width="90" />
</p>

### Reminders & Progress

<p align="center">
  <img src="assets/pdf-screenshots/page7_1_Image69.png" alt="Add water dialog" width="90" />
  <img src="assets/pdf-screenshots/page7_2_Image70.png" alt="Reminder settings dialog" width="90" />
  <img src="assets/pdf-screenshots/page7_3_Image71.png" alt="Hydration reminder notification" width="90" />
  <img src="assets/pdf-screenshots/page7_4_Image72.png" alt="Progress summary screen" width="90" />
</p>

### Settings & Date Picker

<p align="center">
  <img src="assets/pdf-screenshots/page8_1_Image75.png" alt="Date picker screen" width="90" />
  <img src="assets/pdf-screenshots/page8_2_Image76.png" alt="Settings screen" width="90" />
</p>

## 🧰 Tech Stack

| Area | Tools |
| --- | --- |
| Language | Kotlin |
| Platform | Android SDK, minSdk 24, targetSdk 36 |
| UI | Material Components, AppCompat, ConstraintLayout, RecyclerView, CardView |
| Navigation | Fragments, AndroidX Navigation, ViewPager2 |
| Background Work | WorkManager, BroadcastReceivers, alarm scheduling |
| Architecture Helpers | ViewBinding, Lifecycle ViewModel, LiveData |
| Charts | MPAndroidChart |
| Storage | SharedPreferences |
| Testing | JUnit, AndroidX Test, Espresso |

## 🗂️ Project Structure

```text
DailyDo/
├── app/
│   ├── src/main/java/com/dailydo/
│   │   ├── adapter/          # Adapter classes
│   │   ├── data/             # Models and SharedPreferences manager
│   │   ├── receivers/        # Boot and hydration alarm receivers
│   │   ├── services/         # Mood sync service
│   │   ├── ui/               # Auth, fragments, adapters, and chart helpers
│   │   ├── widget/           # Habit progress widget
│   │   └── workers/          # Daily summary and hydration reminder workers
│   └── src/main/res/         # Layouts, drawables, menus, values, and XML configs
├── assets/pdf-screenshots/   # README screenshot assets
├── gradle/                   # Gradle wrapper and version catalog
└── README.md
```

---

<p align="center">
  <strong>DailyDo</strong> - small daily actions, clearer daily progress.
</p>
