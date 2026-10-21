package com.example.myweb.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myweb.R
import com.example.myweb.model.BoardDTO
import com.example.myweb.viewmodel.BoardUiState
import com.example.myweb.viewmodel.BoardViewModel

@Composable
fun BoardApp(viewModel: BoardViewModel) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(viewModel) { viewModel.fetchBoards() }
    BoardContent(state, onRetry = viewModel::fetchBoards)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardContent(state: BoardUiState, onRetry: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.board)) }, actions = {
            TextButton(onClick = onRetry, enabled = state !is BoardUiState.Loading) { Text(stringResource(R.string.refresh)) }
        })
    }) { padding ->
        when (state) {
            BoardUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is BoardUiState.Error -> Column(Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(state.error.messageResource()))
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
            is BoardUiState.Ready -> if (state.boards.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.board_empty))
                }
            } else {
                LazyColumn(contentPadding = padding) {
                    items(state.boards, key = { it.boardNo }) { BoardItem(it) }
                }
            }
        }
    }
}

@Composable
fun BoardItem(board: BoardDTO) {
    OutlinedCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(board.boardTitle, style = MaterialTheme.typography.titleMedium)
            Text(board.boardWriter, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(board.boardRegdate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
