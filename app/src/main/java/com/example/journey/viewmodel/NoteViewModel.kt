package com.example.journey.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.journey.data.Note

class NoteViewModel : ViewModel() {
    private val _notes = mutableStateListOf<Note>()
    val notes: List<Note> get() = _notes
    
    var searchQuery by mutableStateOf("")
    var showAddNoteDialog by mutableStateOf(false)
    
    init {
        // 添加一些示例数据
        _notes.addAll(
            listOf(
                Note(content = "这是第一条笔记内容。\n\n它包含多行文本。"),
                Note(content = "第二条笔记，用于测试卡片显示效果。"),
                Note(content = "这是一条很长的笔记，用于测试文本截断和展开功能。\n\nLorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
            )
        )
    }
    
    fun addNote(content: String) {
        if (content.isNotBlank()) {
            _notes.add(0, Note(content = content))
        }
    }
    
    fun getFilteredNotes(): List<Note> {
        if (searchQuery.isBlank()) {
            return notes
        }
        return notes.filter { it.content.contains(searchQuery, ignoreCase = true) }
    }
}