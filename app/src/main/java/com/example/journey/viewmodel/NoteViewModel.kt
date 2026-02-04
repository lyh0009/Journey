package com.example.journey.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.journey.data.Note
import com.example.journey.data.NoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NoteRepository(application)
    private val _notes = mutableStateListOf<Note>()
    val notes: List<Note> get() = _notes

    var searchQuery by mutableStateOf("")
    var showAddNoteDialog by mutableStateOf(false)

    init {
        // 从本地加载笔记
        viewModelScope.launch {
            repository.notes.first { savedNotes ->
                _notes.clear()
                _notes.addAll(savedNotes)
                // 如果是首次启动（没有笔记），创建新手指南
                if (savedNotes.isEmpty()) {
                    createWelcomeNote()
                }
                true
            }
        }
    }

    /**
     * 创建新手指南笔记
     */
    private fun createWelcomeNote() {
        val welcomeContent = """# 欢迎使用 Journey 📝

这是一款简洁优雅的笔记应用，帮助你记录想法、灵感和生活点滴。

## ✨ 核心功能

**1. 快速记录**
- 点击底部的 + 按钮，随时记录想法
- 支持 Markdown 格式，让笔记更有层次

**2. 标签管理**
- 使用 #标签 形式为笔记添加分类
- 输入 # 时会自动提示已有标签

**3. 笔记编辑**
- 点击笔记进入编辑模式
- 支持修改、导出、删除操作

## 🎨 Markdown 支持

- **加粗文本**：使用 **文字**
- *斜体文本*：使用 *文字*
- ~~删除线~~：使用 ~~文字~~
- ==高亮文本==：使用 ==文字==
- `行内代码`：使用 `文字`
- #标签：使用 #标签名

## 💡 使用技巧

1. 长按笔记可以展开/收起内容
2. 笔记会自动保存，无需担心丢失
3. 支持导出为 Markdown、JSON、TXT 格式

开始记录你的第一篇笔记吧！"""

        val welcomeNote = Note(
            content = welcomeContent,
            tags = listOf("新手指南", "欢迎使用")
        )
        _notes.add(welcomeNote)
        viewModelScope.launch {
            repository.addNote(welcomeNote)
        }
    }

    fun addNote(content: String, tags: List<String> = emptyList()) {
        if (content.isNotBlank()) {
            val newNote = Note(content = content, tags = tags)
            _notes.add(0, newNote)
            // 保存到本地
            viewModelScope.launch {
                repository.addNote(newNote)
            }
        }
    }

    fun deleteNote(noteId: String) {
        _notes.removeAll { it.id == noteId }
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    fun updateNote(updatedNote: Note) {
        val index = _notes.indexOfFirst { it.id == updatedNote.id }
        if (index != -1) {
            _notes[index] = updatedNote
            viewModelScope.launch {
                repository.updateNotes(_notes.toList())
            }
        }
    }

    fun getFilteredNotes(): List<Note> {
        if (searchQuery.isBlank()) {
            return notes
        }
        return notes.filter { it.content.contains(searchQuery, ignoreCase = true) }
    }

    /**
     * 获取所有已保存的唯一标签
     */
    fun getAllTags(): List<String> {
        return _notes
            .flatMap { it.tags }
            .distinct()
            .sorted()
    }
}
