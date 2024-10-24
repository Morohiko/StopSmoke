# StopSmoke

**StopSmoke** is a user-friendly Android application designed to help individuals quit smoking by tracking cigarette consumption, enforcing quitting schedules, and providing motivational support. By leveraging effective habit-forming techniques and comprehensive tracking, StopSmoke empowers users to take control of their smoking habits and lead a healthier lifestyle.

---

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)

---

## Features

- **Cigarette Logging:** Easily log each cigarette you smoke to keep track of your consumption.
- **16-Hour Timer:** Enforce a 16-hour interval between cigarettes to promote longer periods without smoking.
- **Daily Tracker:** Monitor the number of cigarettes smoked each day.
- **Customizable Settings:** Adjust allowed cigarettes per day and reduction rates to tailor the quitting plan to your needs.
- **Clear Cache and History:** Reset your progress by clearing all logs and resetting preferences.
- **Persistent Data:** Ensure your tracking data and timer persist across app restarts and device reboots.
- **Notifications:** Receive reminders and motivational messages to stay on track.
- **User-Friendly Interface:** Intuitive design for seamless user experience.

---

## Getting Started

Follow these instructions to set up and run the StopSmoke application on your local machine for development and testing purposes.

### Prerequisites

- **Android Studio:** Ensure you have the latest version of [Android Studio](https://developer.android.com/studio) installed.
- **Android SDK:** The project is built using Android SDK version 33. Ensure you have this SDK version installed.
- **Java Development Kit (JDK):** Java 11 or higher is recommended.
- **Emulator or Physical Device:** For running and testing the application.

### Installation

1. **Clone the Repository:**

  ```bash
  git clone https://github.com/yourusername/StopSmoke.git
  ```

2. **Open in Android Studio:**

- Launch Android Studio.
- Click on "Open an existing Android Studio project".
- Navigate to the cloned StopSmoke directory and select it.

3. **Sync Gradle:**

- Upon opening the project, Android Studio may prompt you to "Sync Now". Click on it to download all necessary dependencies.
- Alternatively, navigate to "File" > "Sync Project with Gradle Files".

4. **Build the Project:**

- Once Gradle sync is complete, build the project by clicking "Build" > "Make Project" or using the shortcut Ctrl+F9.

5. **Run the Application:**

- Connect your Android device or start an emulator.
- Click the "Run" button or navigate to "Run" > "Run 'app'".
- Select your deployment target and click "OK".

### Usage

1. **Logging a Cigarette:**

- Open the StopSmoke app.
- Tap on the "Log Cigarette" button each time you smoke.
- The "Smoked Today" count will increment accordingly.
- A 16-hour timer will start, disabling the log button until the timer completes.

2. **Viewing Timer:**

- The "Next cigarette allowed in" timer displays the remaining time before you can log another cigarette.

3. **Accessing Settings:**

- Navigate to the Settings screen by tapping the "Settings" button.
- Adjust your allowed cigarettes per day and reduction rates to customize your quitting plan.

4. **Clearing Cache and History:**

- In the Settings screen, tap on "Clear Cache and History".
- Confirm the action to reset your tracking data and preferences.

5. **Receiving Notifications:**

- Ensure that notifications are enabled to receive timely reminders and motivational messages.
