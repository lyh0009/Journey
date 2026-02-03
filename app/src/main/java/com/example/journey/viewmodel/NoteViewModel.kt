package com.example.journey.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.journey.data.Note
import com.example.journey.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    var searchQuery by mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            repository.notes.collect { noteList ->
                _notes.value = noteList
            }
        }
    }

    fun addNote(content: String, tags: List<String> = emptyList()) {
        if (content.isNotBlank()) {
            viewModelScope.launch {
                val note = Note(content = content, tags = tags)
                repository.addNote(note)
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note.id)
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun getFilteredNotes(): List<Note> {
        return if (searchQuery.isBlank()) {
            _notes.value
        } else {
            _notes.value.filter { it.content.contains(searchQuery, ignoreCase = true) }
        }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
                return NoteViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
