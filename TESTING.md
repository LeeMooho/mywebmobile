# Android tests

Use JDK 17 and Android SDK 34. From the repository root:

```sh
bash gradlew --no-daemon --max-workers=2 testDebugUnitTest assembleDebug lintDebug
```

In the prepared cloud environment, first run:

```sh
source /workspace/toolchains/activate.sh
```

The suite runs on the JVM without an emulator or live board server:

- Compose/Robolectric on API 28: English/Korean/Japanese home content, navigation
  selection, action callbacks, scrolling, and dark-theme accessibility.
- Login UI: form submission, masked/visible password, loading state, validation
  and server error messages.
- Settings UI: language changes immediately update navigation, theme selection,
  and navigation to/from the login screen.
- Board UI: loading, empty, populated, error and retry states, list-item selection,
  and detailed title/author/date/content rendering with back navigation.
- ViewModels: input validation, successful/failed login, retry, preservation of
  password whitespace, and duplicate-request prevention.
- HTTPS MockWebServer: login and board-detail API paths, full post mapping, Bearer token delivery, session
  expiration/logout, missing tokens, malformed responses, envelope failures,
  redirect rejection and cleartext credential/token protection. Local test
  certificates are explicitly trusted; TLS and hostname verification stay enabled.
- Preferences: defaults, persistence, invalid stored values, unsupported languages.

The first run downloads Robolectric's Android runtime from Maven Central. Reports
are written to `app/build/reports/tests/testDebugUnitTest/index.html` and
`app/build/reports/lint-results-debug.html`.

The existing sample arithmetic test remains separate from regression coverage.
Robolectric does not replace checking rendering, system integration, and live
login/board retrieval on a real device. Instrumentation tests still require a
connected device or emulator (`bash gradlew connectedDebugAndroidTest`).
See [API_CONFIGURATION.md](API_CONFIGURATION.md) for the server contract and
HTTPS configuration required for real login.
