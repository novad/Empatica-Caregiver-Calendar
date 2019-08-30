
# Caregiver scheduling Android application 
Author: Daniel Nova

Android Application to handle caregivers' schedule in a hospital.
The application allows a user to create `Appointments` for the different caregivers in the hospital, 
each appointment can be assigned to specific 1-hour time-slots based on the date selected by the user.
Appointments consist of a specified

## Dependencies 
* Gradle 5.4.1, available at <https://gradle.org/>
* (_Preferred_) Android Studio 3.5 version 3.5
* Android SDK tools (available at <https://developer.android.com/studio#downloads>) and installed in directory `~/Library/Android/sdk`
* Minimum SDK version 26, target version is 28.
* AndroidX for support libraries.

### Other library dependencies
Specified in the `build.gradle` file.
* Volley 1.1.1 got HTTP request management.
* Glide 4.8.0 for image loading management.
* Room persistence library 2.2.0.
* Horizontal Calendar View made Mulham Raee, Licensed under the Apache License, Version 2.0. Available at <https://github.com/Mulham-Raee/Horizontal-Calendar>

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

## Overview

The final application consists of three main activities which are found under `app/src/main/<package_name>/ui`:
* `MainActivity.java`. The main activity that displays a calendar view to select a date, and a list of 1-hour time-slots indicating the hours of the day. The time-slots indicate the caregivers assigned to different rooms for the selected date and hour. A user can also add/edit information by tapping on the time-slots.
  ** The caregivers auto-fit functionality can be started using "fab" button on the bottom right of the activity.
* `AppointmentActivity.java` Displays the form to create or edit a single appointment.
* `CaregiversActivity.java` Displays a list of caregivers that the user can select. The default behaviour is that the caregivers database is empty, by opening the activity 10 caregivers are loaded from the API and cached in the database. The user can load more caregivers up to 100.



