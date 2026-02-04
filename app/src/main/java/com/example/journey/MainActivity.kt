package com.example.journey

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.journey.data.Note
import com.example.journey.ui.component.AddNoteDialog
import com.example.journey.ui.screen.EditNoteScreen
import com.example.journey.ui.screen.LogsScreen
import com.example.journey.ui.screen.NotesScreen
import com.example.journey.ui.screen.SettingsScreen
import com.example.journey.ui.theme.JourneyTheme
import com.example.journey.viewmodel.NoteViewModel

// 定义导航路由
object Routes {
    const val NOTES = "notes"
    const val SETTINGS = "settings"
    const val LOGS = "logs"
    const val EDIT_NOTE = "edit_note/{noteId}"

    fun editNote(noteId: String) = "edit_note/$noteId"
}

class MainActivity : ComponentActivity() {

    // 导出文件回调
    private var pendingExportContent: String? = null
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                pendingExportContent?.let { content ->
                    try {
                        contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(content.toByteArray())
                        }
                        android.widget.Toast.makeText(
                            this,
                            "导出成功",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(
                            this,
                            "导出失败: ${e.message}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                    pendingExportContent = null
                }
            }
        }
    }

    fun exportToFile(content: String, mimeType: String, defaultFileName: String) {
        pendingExportContent = content
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, defaultFileName)
        }
        createDocumentLauncher.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        setContent {
            JourneyTheme {
                MainScreen(
                    viewModel = viewModel,
                    onExport = { content, mimeType, fileName ->
                        exportToFile(content, mimeType, fileName)
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: NoteViewModel,
    onExport: (String, String, String) -> Unit = { _, _, _ -> }
) {
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
                onLogsClick = {
                    navController.navigate(Routes.LOGS)
                },
                onEditNoteClick = { note ->
                    editingNote = note
                    navController.navigate(Routes.editNote(note.id))
                },
                onExport = onExport
            )
        }
        
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.LOGS) {
            LogsScreen(
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
