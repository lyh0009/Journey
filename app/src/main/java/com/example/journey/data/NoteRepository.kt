package com.example.journey.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notes_preferences")

// 自定义 LocalDateTime TypeAdapter
class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.format(formatter))
        }
    }

    override fun read(input: JsonReader): LocalDateTime? {
        return try {
            LocalDateTime.parse(input.nextString(), formatter)
        } catch (e: Exception) {
            null
        }
    }
}

class NoteRepository(private val context: Context) {
    // 配置 Gson 支持 Java 8 日期时间
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()

    private val notesKey = stringPreferencesKey("notes_list")

    val notes: Flow<List<Note>> = context.dataStore.data
        .map { preferences ->
            val notesJson = preferences[notesKey] ?: "[]"
            val listType = object : TypeToken<List<Note>>() {}.type
            try {
                gson.fromJson<List<Note>>(notesJson, listType) ?: emptyList()
            } catch (e: Exception) {
                // 如果解析失败，返回空列表
                emptyList()
            }
        }

    suspend fun addNote(note: Note) {
        context.dataStore.edit { preferences ->
            val currentNotesJson = preferences[notesKey] ?: "[]"
            val listType = object : TypeToken<List<Note>>() {}.type
            val currentNotes = try {
                gson.fromJson<List<Note>>(currentNotesJson, listType) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            val updatedNotes = listOf(note) + currentNotes
            preferences[notesKey] = gson.toJson(updatedNotes)
        }
    }

    suspend fun deleteNote(noteId: String) {
        context.dataStore.edit { preferences ->
            val currentNotesJson = preferences[notesKey] ?: "[]"
            val listType = object : TypeToken<List<Note>>() {}.type
            val currentNotes = try {
                gson.fromJson<List<Note>>(currentNotesJson, listType) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
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
