# MyRuns
A fitness tracking android application

## Overview
MyRuns is designed to capture and display physical activities using real-time data and user input. The application consists of several labs, each building upon the last, to create a comprehensive fitness tracking app.

## Labs Overview

### Lab 1: The User Profile
- **Objective**: Implement a simple UI for setting up a user profile, including name, email, phone number, gender, and major.
- **Functionality**:
  - Users can input and save their profile.
  - Profile information is reloaded and displayed when the app is reopened.
- **Learning**:
  - Provided understanding of views, activities, and intents.
  - Introduced to Kotlin and its' major features.

### Lab 2: The User Interface (UI)
- **Objective**: Complete all user interfaces and navigation between activities.
- **Functionality**:
  - Main activity with three fragments: Start, History, and Settings.
  - Ability to navigate between different tabs and maintain state during screen rotation.
- **Learning**:
  - Worked with designing pages with XML and implemented multiple view containers.
  - Learned to access storage in Android.

### Lab 3: The Database
- **Objective**: Implement the database to store and retrieve exercise entries.
- **Functionality**:
  - Add exercise entries manually and view them in the history tab.
  - Delete entries from DisplayEntryActivity.
  - Convert and display data in user-preferred units.
- **Learning**:
  - Utilizing SQLite and Room Database.
  - Implementing shared preferences to store key-value pairs as persistent data.

### Lab 4: Google Maps
- **Objective**: Integrate Google Maps for tracking and visualizing GPS traces.
- **Functionality**:
  - Real-time GPS trace drawing on Google Maps.
  - Save GPS traces in the database and visualize history on the map.
- **Learning**:
  - Implementing status bar and notification panel Notifications using Notification Manager.
  - Took use of GoogleMap API to show live user position.
  - Use services as location listeners to calculate attributes such as distance, average speed, current speed, etc, and show it to the user with Live Data.

### Lab 5: Activity Recognition
- **Objective**: Implement activity recognition using accelerometer data.
- **Functionality**:
  - Collect training data and train a machine learning activity classifier.
  - Implement the classifier in the app for accurate activity recognition.
- **Learning**:
  - using Wekalassifer to produce a classifier using tracking input to train model.
  - Making sure user's privacy and permissions are taken.

## Installation and Setup
- Clone the repository: `git clone https://github.com/gbhardwaj00/MyRuns.git`
- Open the project in Android Studio.
- Ensure you have the latest version of the Android SDK installed.
- Build and run the application on an emulator or physical device.

## Contributing
Feel free to contribute to the project by submitting pull requests or raising issues.

## Acknowledgments
- Course Instructors and TAs of CMPT362, SFU.
