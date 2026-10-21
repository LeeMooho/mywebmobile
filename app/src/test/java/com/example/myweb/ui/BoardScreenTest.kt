package com.example.myweb.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.myweb.api.ApiError
import com.example.myweb.model.BoardDTO
import com.example.myweb.ui.theme.MywebTheme
import com.example.myweb.viewmodel.BoardUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "en")
class BoardScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun emptyListShowsEmptyMessage() {
        compose.setContent { MywebTheme { BoardContent(BoardUiState.Ready(emptyList()), {}) } }
        compose.onNodeWithText("No posts yet. Check back soon.").assertIsDisplayed()
        compose.onNodeWithText("Try again").assertDoesNotExist()
    }

    @Test fun errorShowsRetryAndInvokesCallback() {
        var calls = 0
        compose.setContent { MywebTheme { BoardContent(BoardUiState.Error(ApiError.NETWORK), { calls++ }) } }
        compose.onNodeWithText("Check your connection and try again.").assertIsDisplayed()
        compose.onNodeWithText("Try again").performClick()
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test fun loadingDisablesRefresh() {
        compose.setContent { MywebTheme { BoardContent(BoardUiState.Loading, {}) } }
        compose.onNodeWithText("Refresh").assertIsNotEnabled()
        compose.onNodeWithText("No posts yet. Check back soon.").assertDoesNotExist()
    }

    @Test fun loadedBoardShowsTitleWriterAndDate() {
        val board = BoardDTO(1, "A story", "Content", "2026-10-22", "Writer")
        compose.setContent { MywebTheme { BoardContent(BoardUiState.Ready(listOf(board)), {}) } }
        compose.onNodeWithText("A story").assertIsDisplayed()
        compose.onNodeWithText("Writer").assertIsDisplayed()
        compose.onNodeWithText("2026-10-22").assertIsDisplayed()
    }
}
