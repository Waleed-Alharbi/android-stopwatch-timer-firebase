# Stopwatch & Timer Android App

A simple Android application built with Java that includes user authentication, stopwatch functionality, timer functionality, and lap tracking using Firebase.

## App Preview

<p align="center">
  <img src="screenshots/app-preview.png.png" width="900">
</p>

## Overview

This project is an Android mobile application designed to help users track time using a stopwatch and timer.  
The app includes login and signup functionality, stopwatch lap tracking, timer functionality, and Firebase integration.

## Features

- User login and signup
- Stopwatch with start, lap, and reset options
- Timer functionality
- Lap records display
- Clear saved laps
- Firebase Authentication integration
- Firebase Realtime Database integration
- Clean and simple Android interface

## Technologies Used

- Java
- Android Studio
- XML Layouts
- Firebase Authentication
- Firebase Realtime Database
- Gradle

## Project Structure

```text
app/
 └── src/main/
     ├── java/com/example/stopwatchapp/
     ├── res/layout/
     └── AndroidManifest.xml
```

## Important Firebase Note

The original Firebase configuration file was removed from this public repository for security reasons.

Before running the project, you must add your own Firebase configuration file:

```text
app/google-services.json
```

This file should be generated from your own Firebase project.

Without adding your own `google-services.json` file, the project is mainly for code review and preview purposes, and it may not run correctly.

## How to Run

1. Clone the repository:

```bash
git clone https://github.com/Waleed-Alharbi/android-stopwatch-timer-firebase.git
```

2. Open the project in Android Studio.

3. Create your own Firebase project.

4. Enable Firebase Authentication using Email/Password.

5. Enable Firebase Realtime Database.

6. Download your Firebase configuration file:

```text
google-services.json
```

7. Place it inside:

```text
app/google-services.json
```

8. Wait for Gradle Sync to finish.

9. Run the app on an Android emulator or physical device.

## Purpose of the Project

This project was created to practice Android development, Firebase Authentication, Firebase Realtime Database, and basic mobile app UI design.

## Author

Waleed Alharbi
