package com.example.journey.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notes_preferences")

class NoteRepository(private val context: Context) {
    private val gson = Gson()
    private val notesKey = stringPreferencesKey("notes_list")

    val notes: Flow<List<Note>> = context.dataStore.data
        .map { preferences ->
            val notesJson = preferences[notesKey] ?: "[]"
            val listType = object : TypeToken<List<Note>>() {}.type
            gson.fromJson<List<Note>>(notesJson, listType) ?: emptyList()
        }

    suspend fun addNote(note: Note) {
        context.dataStore.edit { preferences ->
            val currentNotesJson = preferences[notesKey] ?: "[]"
            val listType = object : TypeToken<List<Note>>() {}.type
            val currentNotes = gson.fromJson<List<Note>>(currentNotesJson, listType) ?: emptyList()
            val updatedNotes = listOf(note) + currentNotes
            preferences[notesKey] = gson.toJson(updatedNotes)
        }
    }

    suspend fun deleteNote(noteId: String) {
        context.dataStore.edit { preferences ->
            val currentNotesJson = preferences[notesKey] ?: "[]"
            val listType = object : TypeToken<List<Note>>() {}.type
            val currentNotes = gson.fromJson<List<Note>>(currentNotesJson, listType) ?: emptyList()
            val updatedNotes = currentNotes.filter { it.id != noteId }
            preferences[notesKey] = gson.toJson(updatedNotes)
        }
    }

    suspend fun updateNotes(notes: List<Note>) {
        context.dataStore.edit { preferences ->
            preferences[notesKey] = gson.toJson(notes)
        }
    }
}
