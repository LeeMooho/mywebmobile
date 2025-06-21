package com.example.myweb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myweb.api.RetrofitClient
import com.example.myweb.model.BoardDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.lifecycle.viewmodel.compose.viewModel

class BoardViewModel : ViewModel() {
    private val _boardList = MutableStateFlow<List<BoardDTO>>(emptyList())
    val boardList: StateFlow<List<BoardDTO>> = _boardList

    fun fetchBoards() {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.api.getBoardList("all", "")
                if (res.isSuccessful) {
                    _boardList.value = res.body()?.pageItems?.content ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}