package com.example.myweb.ui

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.myweb.MainScreen
import com.example.myweb.ui.theme.MywebTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "en")
class MainScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun bottomNavigationTracksDestinationAndReturnsHome() {
        compose.setContent { MywebTheme { MainScreen() } }
        val home = compose.onNode(hasText("Home") and isSelectable())
        val myPage = compose.onNode(hasText("My Page") and isSelectable())
        home.assertIsSelected()
        myPage.assertIsNotSelected().performClick()
        myPage.assertIsSelected()
        home.assertIsNotSelected().performClick()
        home.assertIsSelected()
        myPage.assertIsNotSelected()
    }

    @Test fun homeSettingsShortcutOpensSettingsAndBackReturnsHome() {
        compose.setContent { MywebTheme { MainScreen() } }
        compose.onNodeWithText("Personalize your experience").performScrollTo().performClick()
        compose.onNodeWithText("Settings").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNode(hasText("Home") and isSelectable()).assertIsSelected()
    }
}
