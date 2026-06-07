# DailyDo — Habit, Mood & Hydration Tracker

<p align="center">
  <img src="https://img.shields.io/badge/Android-24%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android 24+" />
  <img src="https://img.shields.io/badge/Kotlin-1.9%2B-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Material%20Design-Components-018786?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material Design" />
</p>

<p align="center">
  A daily routine app for habits, moods, and hydration with a clean Android experience.
</p>

---

## Description

DailyDo is a dedicated mobile application designed to help users establish and maintain healthy routines, manage emotional well-being, and ensure proper hydration. It provides a practical set of tools for personal growth and daily accountability.

## Key Features

- Habit Tracker: Users can create and monitor personalized habits with visual progress bars and streak tracking to maintain motivation.
- Mood Journal: The emoji-based Mood Journal makes emotional logging simple and supports awareness over time with optional notes.
- Hydration Manager: Users can set a daily intake goal, check their current intake and progress percentage, and quickly log water with customizable amounts.
- Progress Summary: A daily summary screen shows Habit Avg, Mood Avg, and Hydration Avg so the user can review activity by date.
- Advanced Features & Data Persistence: Android SharedPreferences are used for secure and persistent local storage, keeping the app offline-friendly.
- User Management: Signup, Sign In, Settings, Notifications, Hydration settings, About, and Logout are included.

## Project Highlights

- Kotlin-first Android app with a clean modular package structure.
- Material-style UI with custom colors, shapes, and reusable drawables.
- ViewBinding enabled for safer and clearer view access.
- WorkManager and receivers used for reminders, boot handling, and background support.
- Navigation, RecyclerView, ViewPager2, and fragment-based screens keep the flow organized.
- Home screen widget support for quick habit progress checks.

## Tech Stack & Key Dependencies

### Core

- Kotlin — primary language
- Android SDK — targeted at API 24+
- Material Components — UI controls and styling

### Libraries

- AndroidX AppCompat, Core KTX, Activity, ConstraintLayout
- Fragment KTX and Navigation Component
- RecyclerView and CardView
- ViewPager2 for onboarding flows
- WorkManager for reminders and daily summary jobs
- Lifecycle ViewModel and LiveData
- MPAndroidChart for mood and progress charts

## Screenshots

### Launch Screen

<p align="center"><img src="assets/pdf-screenshots/page3_1_Image48.png" alt="Launch screen" width="180" /></p>

### Onboarding and Welcome

<table>
  <tr>
    <td><img src="assets/pdf-screenshots/page4_1_Image51.jpg" alt="Onboarding screen 1" width="180" /></td>
    <td><img src="assets/pdf-screenshots/page4_2_Image52.jpg" alt="Onboarding screen 2" width="180" /></td>
    <td><img src="assets/pdf-screenshots/page4_3_Image53.jpg" alt="Onboarding screen 3" width="180" /></td>
    <td><img src="assets/pdf-screenshots/page4_4_Image54.jpg" alt="Welcome screen" width="180" /></td>
  </tr>
</table>

### Signup, Sign In, Habit

<table>
  <tr>
    <td><img src="assets/pdf-screenshots/page5_1_Image57.png" alt="Signup page" width="180" /></td>
    <td><img src="assets/pdf-screenshots/page5_2_Image58.png" alt="Sign in page" width="180" /></td>
    <td><img src="assets/pdf-screenshots/page5_3_Image59.png" alt="Habit screen" width="180" /></td>
    <td><img src="assets/pdf-screenshots/page5_4_Image60.png" alt="Add habit dialog" width="180" /></td>
  </tr>
</table>

### Mood and Hydration

<table>
  <tr>
    <td><img src="assets/pdf-screenshots/page6_1_Image63.png" alt="Mood screen" width="180" /></td>
    <td><img src="assets/pdf-screenshots/page6_2_Image64.png" alt="Recent moods" width="180" /></td>
    <td><img src="assets/pdf-screenshots/page6_3_Image65.png" alt="Hydration screen" width="180" /></td>
    <td><img src="assets/pdf-screenshots/page6_4_Image66.png" alt="Set goal dialog" width="180" /></td>
  </tr>
</table>

### Reminders and Progress

<table>
  <tr>
    <td><img src="assets/pdf-screenshots/page7_1_Image69.png" alt="Add water dialog" width="180" /></td>
    <td><img src="assets/pdf-screenshots/page7_2_Image70.png" alt="Set reminder dialog" width="180" /></td>
    <td><img src="assets/pdf-screenshots/page7_3_Image71.png" alt="Reminder notification" width="180" /></td>
    <td><img src="assets/pdf-screenshots/page7_4_Image72.png" alt="Progress summary screen" width="180" /></td>
  </tr>
</table>

### Settings and Date Picker

<table>
  <tr>
    <td><img src="assets/pdf-screenshots/page8_1_Image75.png" alt="Pick a date screen" width="180" /></td>
    <td><img src="assets/pdf-screenshots/page8_2_Image76.png" alt="Settings page" width="180" /></td>
  </tr>
</table>
