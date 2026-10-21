# API and authentication

The existing board server is the default: `http://www.mooho.shop:8080/`.
Set a verified HTTPS base URL at build time before using sign-in:

```sh
bash gradlew assembleDebug -PapiBaseUrl=https://your-server.example/
```

The URL must end in `/` and contain no user info, query, or fragment. This setting
is compiled into `BuildConfig.API_BASE_URL`; it must never contain credentials.
Requests preserve any path prefix before the relative API paths below.

## Existing server contract

The adapter follows the companion server at
[LeeMooho/myweb, commit e4c20d5](https://github.com/LeeMooho/myweb/tree/e4c20d581d726bd8909f1abe86bba00ec546b8c5/backend/src/main/java/com/bit/boardapp).
This is source-level compatibility; the deployed server and real credentials
were not tested from this environment.

| Operation | Request | Response envelope |
| --- | --- | --- |
| Login | `POST api/user/login`, JSON `userId` and `userPw` | `statusCode: 200`, `item` containing `userId`, `userName`, `token` |
| Logout | `GET api/user/logout`, Bearer token | `statusCode: 200`, `item` message map |
| Boards | `GET api/board/board-list?searchCondition=all&searchKeyword=` | `statusCode: 200`, `pageItems.content` array |

HTTP status and envelope failures are checked separately. A missing or zero
`errorCode` with a 2xx `statusCode` is accepted, matching the server DTO defaults. Server error codes
200/201 map to invalid credentials. Network failures, 401/403, server failures,
and invalid JSON/envelopes map to localized UI messages. Server error text and
credentials are never logged or shown directly. Coroutine cancellation propagates.

Login refuses cleartext transport before sending credentials. Tokens are sent only
to the configured HTTPS origin, excluded from login calls, and never forwarded
through redirects. The legacy cleartext exception is limited to the existing board
host by Android network security configuration. Other HTTP hosts are blocked by
Android; use HTTPS when changing the API URL.

Sessions are held only in memory. Passwords are not saved in preferences or saved
instance state. Closing the app process requires signing in again. An authenticated
401 clears only the matching session, so a delayed response cannot clear a newer
login. Logout always clears the local session, including on server failure; this
does not guarantee that the server has revoked an already issued token.

## App settings

`SettingsRepository` owns `app_preferences`, preserving the previous `language`
key. Language (English/Korean/Japanese) and theme (system/light/dark) apply
immediately and survive a new app process. Invalid stored settings fall back to
English and the system theme. Preferences do not contain authentication tokens.
`strings-master.xml` includes all localized strings used by the generation script.

## Release signing

Debug builds do not need release credentials. A release build can use
`RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and
`RELEASE_KEY_PASSWORD` supplied securely outside source control. The keystore
must be available locally to the signing tool. Never put secret values in this file
or in Gradle properties committed to Git.
