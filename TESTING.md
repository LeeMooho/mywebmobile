# Android tests

Use JDK 17 and Android SDK 34. From the repository root:

```sh
bash gradlew --no-daemon --max-workers=2 testDebugUnitTest assembleDebug lintDebug
```

In the prepared cloud environment, first run:

```sh
source /workspace/toolchains/activate.sh
```

The local JVM suite includes eight Compose tests backed by Robolectric on API 28:

- Home content in English, Korean, and Japanese.
- Board and settings actions invoke only their intended callback.
- The settings action remains reachable by scrolling.
- Home actions remain accessible in the dark theme.
- Bottom navigation updates its selected destination and returns home.
- The home settings shortcut updates the selected navigation tab.

These tests need no emulator, release signing credentials, or live board server.
The first run downloads Robolectric's Android runtime from Maven Central.
Reports are written to `app/build/reports/tests/testDebugUnitTest/index.html`
and `app/build/reports/lint-results-debug.html`.

The existing sample arithmetic test is separate from the eight UI regression tests.
Robolectric checks do not replace testing rendering, system integration, and live
board retrieval on a real device. Instrumentation tests still require an emulator
or connected Android device (`bash gradlew connectedDebugAndroidTest`).
