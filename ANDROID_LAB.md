# Portfolio Android Lab

The portfolio keeps Android platform experiments behind the **My Page → Android
Lab** flow. Each example is small enough to reuse in another app and separates UI
state from Android side effects.

## Included examples

| Page | Android API | Behavior |
| --- | --- | --- |
| Device information | `Build`, `Configuration` | Shows manufacturer, model, Android/SDK version, screen dp size, density, and locale. It deliberately excludes stable identifiers, accounts, phone numbers, and location. |
| Clipboard | `ClipboardManager` | Copies the public GitHub portfolio URL as plain text. |
| Share sheet | `ACTION_SEND`, `Intent.createChooser` | Lets the user choose where to share a prepared portfolio message. |
| Haptics | Compose `HapticFeedback` | Plays a short feedback effect in direct response to a button press. |
| External profile | `ACTION_VIEW` | Opens a fixed HTTPS GitHub URL through an Android activity. |
| Notification | `NotificationChannel`, `NotificationCompat` | Requests `POST_NOTIFICATIONS` only on Android 13+, creates a channel on Android 8+, posts a local notification, and returns to the app when tapped. |

`AndroidPortfolioActions` owns intents, clipboard access, build/configuration
reads, and notification creation. Compose screens receive callbacks or this
adapter, so the UI can be tested without invoking system UI. Permission handling
stays in the notification route because it is part of the user interaction.

## Safety and extension rules

- Keep public URLs and demo text fixed; do not place credentials or tokens in
  intents, clipboard data, notifications, or logs.
- Prefer user-triggered system UI. Do not open share sheets, permission prompts,
  or external activities during composition.
- Add a clear explanation before a new permission and request it only when the
  related feature is used.
- Avoid stable device identifiers. Add location, contacts, camera, or microphone
  only with a concrete portfolio scenario, an in-context disclosure, denial
  handling, and tests.
- Put each new side effect in the platform adapter and keep its Compose content
  callback-driven. This preserves JVM test coverage and makes device-only tests
  focused.

The Robolectric suite verifies clipboard contents, chooser and VIEW intents,
safe device fields, notification channel/posting, screen navigation, and every
callback. A real device is still needed to judge haptic strength, notification
appearance, installed share targets, and OEM-specific presentation.
