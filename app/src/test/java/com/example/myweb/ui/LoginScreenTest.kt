package com.example.myweb.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.myweb.api.ApiError
import com.example.myweb.ui.theme.MywebTheme
import com.example.myweb.viewmodel.LoginUiState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "en")
class LoginScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun submittingFormUsesEnteredCredentials() {
        var submitted: Pair<String, String>? = null
        compose.setContent { MywebTheme { LoginScreen(LoginUiState(), { id, pw -> submitted = id to pw }, {}) } }
        compose.onNodeWithTag("login_user_id").performTextInput("my-id")
        compose.onNodeWithTag("login_password").performTextInput("my-password")
        compose.onNodeWithTag("login_submit").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("my-id" to "my-password", submitted) }
    }

    @Test fun passwordIsMaskedUntilShowIsSelected() {
        compose.setContent { MywebTheme { LoginScreen(LoginUiState(), { _, _ -> }, {}) } }
        compose.onNodeWithTag("login_password").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
        compose.onNodeWithText("Show").performScrollTo().performClick()
        compose.onNodeWithTag("login_password").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Password))
        compose.onNodeWithText("Hide").performClick()
        compose.onNodeWithTag("login_password").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
    }

    @Test fun loadingDisablesFormAndSubmission() {
        compose.setContent { MywebTheme { LoginScreen(LoginUiState(loading = true), { _, _ -> fail("Must not submit") }, {}) } }
        compose.onNodeWithTag("login_user_id").assertIsNotEnabled()
        compose.onNodeWithTag("login_password").assertIsNotEnabled()
        compose.onNodeWithTag("login_submit").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Signing in…").assertIsDisplayed()
    }

    @Test fun rejectedCredentialsShowLocalizedError() {
        compose.setContent { MywebTheme { LoginScreen(LoginUiState(error = ApiError.INVALID_CREDENTIALS), { _, _ -> }, {}) } }
        compose.onNodeWithText("The user ID or password is incorrect.").performScrollTo().assertIsDisplayed()
    }

    @Test fun blankFormErrorIsShown() {
        compose.setContent { MywebTheme { LoginScreen(LoginUiState(invalidInput = true), { _, _ -> }, {}) } }
        compose.onNodeWithText("Enter your user ID and password.").performScrollTo().assertIsDisplayed()
    }
}
