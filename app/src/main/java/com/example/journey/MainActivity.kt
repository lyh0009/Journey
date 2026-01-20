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
import com.example.journey.ui.component.AddNoteDialog
import com.example.journey.ui.screen.NotesScreen
import com.example.journey.ui.screen.SettingsScreen
import com.example.journey.ui.theme.JourneyTheme
import com.example.journey.viewmodel.NoteViewModel

// 定义导航路由
object Routes {
    const val NOTES = "notes"
    const val SETTINGS = "settings"
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
    }
    
    // Show Add Note Dialog
    if (showAddNoteDialog) {
        AddNoteDialog(
            onDismiss = {
                showAddNoteDialog = false
            },
            onSaveNote = {
                viewModel.addNote(it)
                showAddNoteDialog = false
            }
        )
    }
}