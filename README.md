
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

To see the testing routines used (both unit tests and android tests) check the `~app/src/test` and `~app/src/androidTest` folders respectively.

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
The application is divided into 6 main packages:
* `model`. Contains the model classes for appointments, caregivers, and the hospital time-slots. This package also contains models for different database queries results (i.e. `CaregiverWAppointments.java` and `CountWork.java`).
* `ui`. Contains the activities classes.
* `adapter`. Contains the viewadapters classes for the views used in the activities.
* `viewmodel`. ViewModel classes that loads data from the repository to the associated views.
* `persistence`. Contains classes that define the database and the repositories and DAOs for the appointments/caregivers objects.
* `api`. Contains the classes that handle the API calls to `randomuser.me` to fetch the list of caregivers.


The final application consists of three main activities which are found under folder `app/src/main/<package_name>/ui`:
* `MainActivity.java`. The main activity that displays a calendar view to select a date, and a list of 1-hour time-slots indicating the hours of the day. The time-slots indicate the caregivers assigned to different rooms for the selected date and hour. A user can also add/edit information by tapping on the time-slots.
  ** The caregivers auto-fit functionality can be started using "fab" button on the bottom right of the activity.
* `AppointmentActivity.java` Displays the form to create or edit a single appointment.
* `CaregiversActivity.java` Displays a list of caregivers that the user can select. The default behaviour is that the caregivers database is empty, by opening the activity 10 caregivers are loaded from the API and cached in the database. The user can load more caregivers up to 100.

### Auto-fit feature
Allows a user to automatically add the best-suited caregivers to fill all time-slots for a given day.
For each available time-slot and room, the app computes a score for each available caregiver, based on their hours worked and rooms they are working on for the day (if applicable). The best caregiver with the highest score is chosen.

The auto-fit caregivers feature is implemented in an async task under the `AutoFitOperationTask.java` class defined in the main package.


The algorithm works as follows:
1. Fetch all caregivers from the API, so the feature uses all available caregivers from the hospital. If the API is unavailable the cached caregivers in the database are used.
2. For each time-slot of the work day (9:00 to 17:00):
    * Get the list of available rooms for that time-slot
    * For each available room (maximum 10):
        * Create a list of caregivers candidates with scores initialized to 0.
        * Remove all caregivers already working at that hour in the current day
        * Get the list of the caregivers from the repository that have worked the least amount of hours for the last 4 weeks.
        * For each candidate:
            * Get the number of hours worked in a day
               * If the candidate exceeds the maximum hours of 5 + 1 over time, the caregiver is discarded from consideration.
               * If the candidate has no over-time, add a score of 1.
               * If the candidate has appointments for the current day, add a score based on the proximity of the closest room. The score is computed as 2/distance, so a maximum score of 2 can be added.
               * If the candidate is in the list of caregivers with least hours, add a score of 3.
        * The candidate with the highest score is added to the current time-slot and room, a new appointment is created and saved in the repository. If more than one candidate have the same score, the first in the list is chosen.
