
# Caregiver scheduling Android application 
Author: Daniel Nova

Android Application to handle caregivers' schedule in a hospital.

## Dependencies 
* Gradle 5.4.1, available at <https://gradle.org/>
* (_Preferred_) Android Studio 3.5 version 3.5
* Android SDK tools (available at <https://developer.android.com/studio#downloads>) and installed in directory `~/Library/Android/sdk`
* Minimum SDK version 26, target version is 28.
* AndroidX for support libraries.

## Running the application
* (_Preferred_) The application can be directly built and run in Android studio to be tested on an emulator or device.
* Alternatively, to build a debug APK to install on a device, just run `./gradlew assembleDebug`  on the command line. To install it directly on a device run command `./gradlew installDebug`. Complete instructions on building from a command line can be found at <https://developer.android.com/studio/build/building-cmdline>. The resulting APK can be found in `<project directory>/app/build/outputs`


## Structure
```
+-- app/
|   +-- build.gradle
|   +-- src
|       +-- main
|       +-- androidTest
|       +-- test
+-- build.gradle
+-- gradlew
+-- settings.gradle
```
The main source code can be found at `app/src/main/`.
Unit tests can be found at `app/test/`.
Android instrumentality tests can be found at `app/androidTest`.


## Acknowledgments
Horizontal Calendar View made Mulham Raee, Licensed under the Apache License, Version 2.0.
Available at <https://github.com/Mulham-Raee/Horizontal-Calendar>

