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
        // 添加示例数据，展示各种 Markdown 语法
        _notes.addAll(
            listOf(
                Note(
                    content = """欢迎使用 Journey 笔记

这是一段**加粗**的文本，还有*斜体*和~~删除线~~。

1. 这是有序列表
2. 这是2

- 这是无序列表
- 又一个无序列表

## 功能特性

- 支持**加粗**，*斜体*，~~删除线~~，==高亮==，<u>下划线</u>，
- 实时预览编辑效果
- 标签管理

""",
                    tags = listOf("欢迎使用", "Markdown"),
                    createdAt = java.time.LocalDateTime.now()
                ),
                Note(
                    content = """这是一条很长的笔记，用于测试文本截断和展开功能。

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. 

Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. 

Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. 

Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.

Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.

Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt.""",
                    tags = listOf("测试"),
                    createdAt = java.time.LocalDateTime.now().minusDays(4)
                ),
                Note(
                    content = "这是一条短文本，用于测试是否显示‘展开’功能",
                    tags = listOf("测试"),
                    createdAt = java.time.LocalDateTime.now().minusDays(4)
                ),
            )
        )
    }
    
    fun addNote(content: String, tags: List<String> = emptyList()) {
        if (content.isNotBlank()) {
            _notes.add(0, Note(content = content, tags = tags))
        }
    }
    
    fun getFilteredNotes(): List<Note> {
        if (searchQuery.isBlank()) {
            return notes
        }
        return notes.filter { it.content.contains(searchQuery, ignoreCase = true) }
    }
}
