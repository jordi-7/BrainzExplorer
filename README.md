# Brainz Explorer

An Android app for searching artists and browsing their release groups (albums, EPs, singles...) using the public [MusicBrainz](https://musicbrainz.org/) API.

## Tech stack

- **Language / UI**: Kotlin, Jetpack Compose, Material 3
- **DI**: Hilt
- **Networking**: Retrofit, OkHttp, kotlinx.serialization
- **Async**: Kotlin Coroutines / Flow
- **Images**: Coil 3
- **Local storage**: DataStore Preferences (persists sort order)
- **Logging**: Timber

Build config: min SDK 29, target/compile SDK 36, Kotlin 2.4.10, AGP 9.3.0, JVM toolchain 21 (auto-provisioned by Gradle).

## Architecture

The app follows an MVVM structure under `app/src/main/java/com/jordigordillo/brainzexplorer`:

- `data/` — remote DTOs and API interface (`MusicBrainzApi`), OkHttp interceptors, mappers, repository implementation, and local preferences.
- `domain/` — domain models and the `ArtistRepository` interface consumed by the UI layer.
- `ui/` — Compose screens, state holders (`*ViewModel` + `*UiState`) and reusable components, organized per feature (`home`, `detail`).
- `navigation/` — Compose Navigation graph and destinations.
- `di/` — Hilt modules wiring networking and repositories.

## Requirements

- JDK 21 (provisioned automatically via the Gradle toolchain)
- Android Studio (latest stable) or the Gradle command line
- An Android device or emulator running API 29+

## Build & run

Open the project in Android Studio, let it sync, and run the `app` configuration — or use the command line:

```bash
./gradlew assembleDebug     # build a debug APK
./gradlew installDebug      # build and install on a connected device/emulator
```

No API keys or secrets are required.

## Testing

```bash
./gradlew testDebugUnitTest         # unit tests (JUnit4, MockK, Turbine, Robolectric)
./gradlew connectedDebugAndroidTest # instrumented tests (Espresso, Compose UI test, Hilt testing) — requires a connected device/emulator
```

## API notes

The app talks directly to `https://musicbrainz.org/ws/2/` — no API key is needed. Two OkHttp interceptors keep usage compliant with MusicBrainz's terms:

- `UserAgentInterceptor` sets a descriptive `User-Agent` header identifying the app and a contact address.
- `RateLimitInterceptor` throttles requests to respect MusicBrainz's public rate limit.

## Permissions

- `INTERNET` — required to query the MusicBrainz API.
