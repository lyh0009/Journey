package com.example.journey.ui.screen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.journey.data.Note
import com.example.journey.ui.component.NoteCard
import com.example.journey.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NoteViewModel,
    onAddNoteClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val filteredNotes = viewModel.getFilteredNotes()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "一日笔记")
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClick,
                containerColor = Color(0xFF64B5F6),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,      // 默认状态无阴影
                    pressedElevation = 0.dp,      // 按下时无阴影
                    focusedElevation = 0.dp,      // 聚焦时无阴影
                    hoveredElevation = 0.dp       // 悬停时无阴影
                ),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "添加笔记",
                    modifier = Modifier.size(40.dp) // 图标尺寸
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            // Search Bar 搜索栏
            TextField(
                value = viewModel.searchQuery,
                onValueChange = { query -> viewModel.searchQuery = query },
                placeholder = {
                    Text(
                        text = "搜索笔记...",
                        style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                    )
                },
                leadingIcon = {
                    IconButton(onClick = onSettingsClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "设置",
                            tint = Color.Gray
                        )
                    }
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "搜索",
                        modifier = Modifier.size(28.dp),
                        tint = Color.Gray
                    )
                },
                // 输入框样式
                modifier = Modifier
                    .padding(
                    start = 16.dp,// 减小左边间距，让它离屏幕更近一些
                    end = 16.dp,// 减小右边间距，让它离屏幕更近一些
                    top = 16.dp,// 减小顶部间距，让它离屏幕更近一些
                    bottom = 0.dp // 减小底部间距，让它离列表更近一些
                )
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.LightGray.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.LightGray.copy(alpha = 0.2f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            
            // Notes List 笔记列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // 列表内间距
                contentPadding = PaddingValues(16.dp),
                // 多条笔记间隔
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredNotes) {
                    NoteCard(note = it)
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
@Suppress("ViewModelConstructorInComposable")
fun NotesScreenPreview() {
    // For preview, we can directly create a ViewModel instance
    // This is acceptable for preview purposes only
    val viewModel = NoteViewModel()
    
    NotesScreen(
        viewModel = viewModel,
        onAddNoteClick = {},
        onSettingsClick = {}
    )
}