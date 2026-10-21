package com.example.myweb.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myweb.R
import com.example.myweb.viewmodel.LoginUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(state: LoginUiState, onLogin: (String, String) -> Unit, onBack: () -> Unit) {
    // Credentials deliberately stay out of saved instance state and preferences.
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    fun submit() { if (!state.loading) onLogin(userId, password) }

    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.login)) }, navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
            }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("MYWEB", color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge)
            Text(stringResource(R.string.login_welcome), style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.login_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(userId, { userId = it }, modifier = Modifier.fillMaxWidth().testTag("login_user_id"),
                label = { Text(stringResource(R.string.user_id)) }, singleLine = true, enabled = !state.loading,
                isError = state.invalidInput && userId.isBlank(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
            OutlinedTextField(password, { password = it }, modifier = Modifier.fillMaxWidth().testTag("login_password"),
                label = { Text(stringResource(R.string.password)) }, singleLine = true, enabled = !state.loading,
                isError = state.invalidInput && password.isBlank(),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                trailingIcon = {
                    TextButton(onClick = { showPassword = !showPassword }, enabled = !state.loading) {
                        Text(stringResource(if (showPassword) R.string.hide_password else R.string.show_password))
                    }
                })
            if (state.invalidInput || state.error != null) {
                Text(stringResource(if (state.invalidInput) R.string.login_required else state.error!!.messageResource()),
                    color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("login_error"))
            }
            Button(onClick = { submit() }, enabled = !state.loading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("login_submit")) {
                if (state.loading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                }
                Text(stringResource(if (state.loading) R.string.signing_in else R.string.login))
            }
        }
    }
}
