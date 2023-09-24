# Android

This repository is part of [android-sms-server](https://github.com/robinchew/android-sms-server).

## How to build

- On Mac or Linux:

```bash
./gradlew *task-name*
```

- On Windows:

```bash
gradlew *task-name*
```

To see a list of available build tasks, execute `tasks`:

```
// on Windows
gradlew tasks

// on Mac or Linux
./gradlew tasks
```

For full documentation, refer to the official documentation on [Build your app from the command line](https://developer.android.com/build/building-cmdline).

## How to build a debug APK

To build a debug APK, open a command line and navigate to the root of this project directory (within `android` directory). To initiate a debug build, invoke the `assembleDebug` task:

```bash
gradlew assembleDebug
```

This creates an APK named `module_name-debug.apk` or in this case `app-debug.apk` in `android/app/build/outputs/apk/`.
