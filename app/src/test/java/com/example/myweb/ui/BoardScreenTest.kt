package com.example.myweb.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.myweb.api.ApiError
import com.example.myweb.model.BoardDTO
import com.example.myweb.ui.theme.MywebTheme
import com.example.myweb.viewmodel.BoardDetailUiState
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

    @Test fun clickingBoardReturnsItsIdentifier() {
        val board = BoardDTO(42, "A story", "Content", "2026-10-22", "Writer")
        var selected: Long? = null
        compose.setContent {
            MywebTheme {
                BoardContent(
                    state = BoardUiState.Ready(listOf(board)),
                    onRetry = {},
                    onBoardClick = { selected = it }
                )
            }
        }
        compose.onNodeWithTag("board_item_42").performClick()
        compose.runOnIdle { assertEquals(42L, selected) }
    }

    @Test fun detailShowsTitleMetadataAndCompleteContent() {
        val board = BoardDTO(
            42,
            "A detailed story",
            "First line\nSecond line",
            "2026-10-22",
            "Writer"
        )
        compose.setContent {
            MywebTheme {
                BoardDetailContent(BoardDetailUiState.Ready(board), onBack = {}, onRetry = {})
            }
        }
        compose.onNodeWithText("Post details").assertIsDisplayed()
        compose.onNodeWithText("A detailed story").assertIsDisplayed()
        compose.onNodeWithText("Writer").assertIsDisplayed()
        compose.onNodeWithText("2026-10-22").assertIsDisplayed()
        compose.onNodeWithText("First line\nSecond line").assertIsDisplayed()
    }

    @Test fun detailBackAndRetryInvokeTheirCallbacks() {
        var backCalls = 0
        var retryCalls = 0
        compose.setContent {
            MywebTheme {
                BoardDetailContent(
                    BoardDetailUiState.Error(ApiError.NETWORK),
                    onBack = { backCalls++ },
                    onRetry = { retryCalls++ }
                )
            }
        }
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("Try again").performClick()
        compose.runOnIdle {
            assertEquals(1, backCalls)
            assertEquals(1, retryCalls)
        }
    }
}
