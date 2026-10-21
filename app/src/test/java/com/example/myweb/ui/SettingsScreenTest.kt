package com.example.myweb.ui

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.myweb.AppRoot
import com.example.myweb.data.AppContainer
import com.example.myweb.data.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "en")
class SettingsScreenTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var container: AppContainer
    @Before fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE).edit().clear().commit()
        container = AppContainer(context)
    }
    private fun openSettings() {
        compose.setContent { AppRoot(container) }
        compose.onNode(hasText("Settings") and isSelectable()).performClick()
    }

    @Test fun choosingKoreanImmediatelyUpdatesNavigationAndSavesPreference() {
        openSettings()
        compose.onNodeWithText("한국어").performScrollTo().performClick()
        compose.onNode(hasText("설정") and isSelectable()).assertIsSelected()
        compose.onNodeWithText("화면 테마").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals("ko", container.settings.settings.value.language) }
    }

    @Test fun choosingDarkThemeUpdatesSelectionAndPreference() {
        openSettings()
        compose.onNodeWithText("Dark").performScrollTo().performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(ThemeMode.DARK, container.settings.settings.value.theme) }
    }

    @Test fun accountOpensLoginAndBackReturnsToSettings() {
        openSettings()
        compose.onNodeWithText("Sign in").performScrollTo().performClick()
        compose.onNodeWithTag("login_user_id").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNode(hasText("Settings") and isSelectable()).assertIsSelected()
    }
}
