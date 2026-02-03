package com.example.journey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.journey.data.Note
import com.example.journey.ui.component.AddNoteDialog
import com.example.journey.ui.screen.EditNoteScreen
import com.example.journey.ui.screen.NotesScreen
import com.example.journey.ui.screen.SettingsScreen
import com.example.journey.ui.theme.JourneyTheme
import com.example.journey.viewmodel.NoteViewModel

// 定义导航路由
object Routes {
    const val NOTES = "notes"
    const val SETTINGS = "settings"
    const val EDIT_NOTE = "edit_note/{noteId}"
    
    fun editNote(noteId: String) = "edit_note/$noteId"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val viewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        
        setContent {
            JourneyTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: NoteViewModel) {
    val navController = rememberNavController()
    var showAddNoteDialog by remember {
        mutableStateOf(false)
    }
    
    // 当前编辑的笔记
    var editingNote by remember { mutableStateOf<Note?>(null) }
    
    NavHost(
        navController = navController,
        startDestination = Routes.NOTES
    ) {
        composable(Routes.NOTES) {
            NotesScreen(
                viewModel = viewModel,
                onAddNoteClick = {
                    showAddNoteDialog = true
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                },
                onEditNoteClick = { note ->
                    editingNote = note
                    navController.navigate(Routes.editNote(note.id))
                }
            )
        }
        
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Routes.EDIT_NOTE) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            val note = editingNote ?: viewModel.notes.find { it.id == noteId }
            
            if (note != null) {
                EditNoteScreen(
                    note = note,
                    onBackClick = {
                        navController.popBackStack()
                        editingNote = null
                    },
                    onSaveNote = { content, tags ->
                        viewModel.updateNote(note.copy(content = content, tags = tags))
                        navController.popBackStack()
                        editingNote = null
                    }
                )
            } else {
                // 如果找不到笔记，返回列表页
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
    }
    
    // Show Add Note Dialog
    if (showAddNoteDialog) {
        AddNoteDialog(
            onDismiss = {
                showAddNoteDialog = false
            },
            onSaveNote = {
                content, tags ->
                viewModel.addNote(content, tags)
                showAddNoteDialog = false
            }
        )
    }
}
