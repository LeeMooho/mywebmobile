package com.example.myweb.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.myweb.R
import com.example.myweb.data.UserSession
import com.example.myweb.platform.DeviceSnapshot
import com.example.myweb.ui.theme.MywebTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "en")
class PortfolioScreensTest {
    @get:Rule val compose = createComposeRule()

    @Test fun myPagePresentsPortfolioAndRoutesToItsDestinations() {
        var labCalls = 0
        var settingsCalls = 0
        var githubCalls = 0
        compose.setContent {
            MywebTheme {
                MyPageScreen(
                    session = null,
                    signingOut = false,
                    onLogin = {},
                    onLogout = {},
                    onOpenSettings = { settingsCalls++ },
                    onOpenAndroidLab = { labCalls++ },
                    onOpenGitHub = { githubCalls++ }
                )
            }
        }
        compose.onNodeWithText("Lee Mooho").assertIsDisplayed()
        compose.onNodeWithText("MyWeb Mobile").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("open_github").performClick()
        compose.onNodeWithTag("open_android_lab").performScrollTo().performClick()
        compose.onNodeWithTag("open_app_settings").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(1, githubCalls)
            assertEquals(1, labCalls)
            assertEquals(1, settingsCalls)
        }
    }

    @Test fun myPageShowsSignedInAccountAndLogsOut() {
        var logoutCalls = 0
        compose.setContent {
            MywebTheme {
                MyPageScreen(
                    session = UserSession("mooho", "Mooho", "redacted-token"),
                    signingOut = false,
                    onLogin = {},
                    onLogout = { logoutCalls++ },
                    onOpenSettings = {},
                    onOpenAndroidLab = {},
                    onOpenGitHub = {}
                )
            }
        }
        compose.onNodeWithText("Signed in as Mooho").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Sign out").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, logoutCalls) }
    }

    @Test fun androidLabRoutesToDeviceAndSystemExamples() {
        var deviceCalls = 0
        var systemCalls = 0
        compose.setContent {
            MywebTheme {
                AndroidLabScreen(
                    onBack = {},
                    onOpenDeviceInfo = { deviceCalls++ },
                    onOpenSystemTools = { systemCalls++ }
                )
            }
        }
        compose.onNodeWithTag("open_device_info").performClick()
        compose.onNodeWithTag("open_system_tools").performClick()
        compose.runOnIdle {
            assertEquals(1, deviceCalls)
            assertEquals(1, systemCalls)
        }
    }

    @Test fun devicePageShowsSafeSnapshotFields() {
        val snapshot = DeviceSnapshot("Acme", "Model One", "14", 34, 412, 915, 420, "en-US")
        compose.setContent { MywebTheme { DeviceInfoScreen(snapshot, onBack = {}) } }
        listOf("Acme", "Model One", "14", "34", "412 × 915 dp", "420", "en-US").forEach {
            compose.onNodeWithText(it).performScrollTo().assertIsDisplayed()
        }
        compose.onNodeWithText("phone number", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test fun systemToolsExposeEveryCallbackAndFeedback() {
        val calls = mutableListOf<String>()
        compose.setContent {
            MywebTheme {
                SystemToolsContent(
                    onBack = { calls += "back" },
                    onCopy = { calls += "copy" },
                    onShare = { calls += "share" },
                    onHaptic = { calls += "haptic" },
                    onNotification = { calls += "notification" },
                    onOpenGitHub = { calls += "github" },
                    feedbackResource = R.string.portfolio_link_copied
                )
            }
        }
        compose.onNodeWithContentDescription("Back").performClick()
        listOf(
            "copy_portfolio_link" to "copy",
            "share_portfolio" to "share",
            "play_haptic" to "haptic",
            "show_notification" to "notification",
            "open_github_from_lab" to "github"
        ).forEach { (tag, _) -> compose.onNodeWithTag(tag).performScrollTo().performClick() }
        compose.onNodeWithText("Portfolio link copied.").performScrollTo().assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(listOf("back", "copy", "share", "haptic", "notification", "github"), calls)
        }
    }
}
