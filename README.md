# YiRae

YiRae is an Android photo-memory journal app. It helps users save photos, people, places, and story notes in a private local scrapbook experience.

## Features

- Create memory stories with title, date, place, people, text, and one or more images
- Browse stories from the home screen, favorites view, and calendar view
- Search memories by keyword
- View story details with image switching and story switching
- Share stories through Android share sheets
- Store personal settings locally with app lock and privacy-focused behavior
- Load remote sample content for demo and testing scenarios

## Tech Stack

- Android app with Java
- Gradle Kotlin DSL
- OkHttp for network requests
- Gson for JSON parsing
- Glide for image loading

## Project Structure

- `app/src/main/java/com/example/yirae`: application logic and activities
- `app/src/main/res`: layouts, strings, colors, and drawables
- `gradle/libs.versions.toml`: dependency versions

## Getting Started

### Requirements

- Android Studio
- Android SDK 24+
- JDK 11

### Run Locally

1. Open the project in Android Studio.
2. Sync Gradle.
3. Run the `app` configuration on an emulator or Android device.

## Release Signing

Release signing is intentionally not stored in this repository.

Configure these values in your local Gradle properties or environment variables before building a signed release:

- `YIRAE_RELEASE_STORE_FILE`
- `YIRAE_RELEASE_STORE_PASSWORD`
- `YIRAE_RELEASE_KEY_ALIAS`
- `YIRAE_RELEASE_KEY_PASSWORD`

Example local configuration:

```properties
YIRAE_RELEASE_STORE_FILE=keystore/yirae-release.jks
YIRAE_RELEASE_STORE_PASSWORD=your-password
YIRAE_RELEASE_KEY_ALIAS=yirae-release
YIRAE_RELEASE_KEY_PASSWORD=your-password
```

## Privacy

User nickname, app lock password, and memory content are designed to stay on-device unless the user explicitly shares content.
