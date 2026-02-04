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
                true
            }
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
