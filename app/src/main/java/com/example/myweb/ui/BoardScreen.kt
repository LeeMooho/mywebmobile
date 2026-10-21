package com.example.myweb.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myweb.R
import com.example.myweb.model.BoardDTO
import com.example.myweb.viewmodel.BoardDetailUiState
import com.example.myweb.viewmodel.BoardDetailViewModel
import com.example.myweb.viewmodel.BoardUiState
import com.example.myweb.viewmodel.BoardViewModel

@Composable
fun BoardApp(viewModel: BoardViewModel, onBoardClick: (Long) -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(viewModel) { viewModel.fetchBoards() }
    BoardContent(state, onRetry = viewModel::fetchBoards, onBoardClick = onBoardClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardContent(
    state: BoardUiState,
    onRetry: () -> Unit,
    onBoardClick: (Long) -> Unit = {}
) {
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
                    items(state.boards, key = { it.boardNo }) { board ->
                        BoardItem(board, onClick = { onBoardClick(board.boardNo) })
                    }
                }
            }
        }
    }
}

@Composable
fun BoardItem(board: BoardDTO, onClick: () -> Unit = {}) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("board_item_${board.boardNo}").clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(board.boardTitle, style = MaterialTheme.typography.titleMedium)
            Text(board.boardWriter, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(board.boardRegdate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BoardDetailApp(viewModel: BoardDetailViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(viewModel) { viewModel.fetchBoard() }
    BoardDetailContent(state, onBack = onBack, onRetry = viewModel::fetchBoard)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardDetailContent(
    state: BoardDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.board_detail)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
        )
    }) { padding ->
        when (state) {
            BoardDetailUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            is BoardDetailUiState.Error -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(state.error.messageResource()))
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
            is BoardDetailUiState.Ready -> Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(state.board.boardTitle, style = MaterialTheme.typography.headlineMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        state.board.boardWriter,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        state.board.boardRegdate,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Divider()
                Text(
                    stringResource(R.string.board_content),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(state.board.boardContent, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
