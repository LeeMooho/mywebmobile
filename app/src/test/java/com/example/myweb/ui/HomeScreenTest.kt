package com.example.myweb.ui

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import com.example.myweb.ui.theme.MywebTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class HomeScreenTest {
    @get:Rule val compose = createComposeRule()

    private fun showHome(
        language: String = "en",
        darkTheme: Boolean = false,
        onBoardClick: () -> Unit = {},
        onSettingsClick: () -> Unit = {}
    ) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = Configuration(context.resources.configuration).apply {
            setLocale(Locale(language))
        }
        val localizedContext = context.createConfigurationContext(config)
        compose.setContent {
            CompositionLocalProvider(LocalContext provides localizedContext) {
                MywebTheme(darkTheme = darkTheme) {
                    HomeScreen(onBoardClick, onSettingsClick)
                }
            }
        }
    }

    @Test fun englishHomeShowsLocalizedContent() {
        showHome()
        compose.onNodeWithText("Your everyday space").assertIsDisplayed()
        compose.onNodeWithText("Explore the board").performScrollTo().assertIsDisplayed()
    }

    @Test fun koreanHomeShowsLocalizedContent() {
        showHome(language = "ko")
        compose.onNodeWithText("나의 일상, 새로운 연결").assertIsDisplayed()
        compose.onNodeWithText("게시판 둘러보기").performScrollTo().assertIsDisplayed()
    }

    @Test fun japaneseHomeShowsLocalizedContent() {
        showHome(language = "ja")
        compose.onNodeWithText("日常に、新しいつながりを").assertIsDisplayed()
        compose.onNodeWithText("掲示板を見る").performScrollTo().assertIsDisplayed()
    }

    @Test fun exploreButtonOnlyInvokesBoardCallback() {
        var boardClicks = 0
        var settingsClicks = 0
        showHome(onBoardClick = { boardClicks++ }, onSettingsClick = { settingsClicks++ })
        compose.onNodeWithText("Explore the board").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(1, boardClicks)
            assertEquals(0, settingsClicks)
        }
    }

    @Test fun settingsButtonCanBeReachedByScrollingAndOnlyInvokesSettingsCallback() {
        var boardClicks = 0
        var settingsClicks = 0
        showHome(onBoardClick = { boardClicks++ }, onSettingsClick = { settingsClicks++ })
        compose.onNodeWithText("Personalize your experience").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(0, boardClicks)
            assertEquals(1, settingsClicks)
        }
    }

    @Test fun darkThemeKeepsHomeActionsAccessible() {
        showHome(darkTheme = true)
        compose.onNodeWithText("Your everyday space").assertIsDisplayed()
        compose.onNodeWithText("Personalize your experience").performScrollTo().assertIsDisplayed()
    }
}
