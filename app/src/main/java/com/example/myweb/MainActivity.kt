package com.example.myweb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myweb.model.BoardDTO
import com.example.myweb.viewmodel.BoardViewModel

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BoardApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardApp(viewModel: BoardViewModel = viewModel()) {
    val boards by viewModel.boardList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchBoards()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("게시판 목록") })
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(boards) { board ->
                BoardItem(board)
            }
        }
    }
}

@Composable
fun BoardItem(board: BoardDTO) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Text(text = board.boardTitle, style = MaterialTheme.typography.titleMedium)
        Text(text = board.boardWriter, style = MaterialTheme.typography.labelMedium)
        Text(text = board.boardRegdate, style = MaterialTheme.typography.labelSmall)
    }
}