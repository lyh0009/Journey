package com.example.journey.ui.screen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.journey.data.Note
import com.example.journey.ui.component.NoteCard
import com.example.journey.ui.theme.LocalCustomColors
import com.example.journey.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NoteViewModel,
    onAddNoteClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onEditNoteClick: (Note) -> Unit = {}
) {
    val filteredNotes = viewModel.getFilteredNotes()
    var isSearchVisible by remember { mutableStateOf(false) }
    val customColors = LocalCustomColors.current
    
    // Drawer state management 抽屉状态管理
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    // 1. 获取窗口宽度（单位是像素）
    val containerWidthPx = windowInfo.containerSize.width

    // 2. 根据比例计算侧边栏宽度（例如 Telegram 风格的 80%）
    // 3. 将像素转换回 Dp，以便在 ModalDrawerSheet 中使用
    val drawerWidthDp = with(density) {
        (containerWidthPx * 0.8f).toDp()
    }
    // 抽屉导航栏
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidthDp),
                drawerShape = RectangleShape
            ) {
                // Drawer header
                Text(
                    text = "Notes",
                    modifier = Modifier.padding(16.dp),
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Drawer items
                NavigationDrawerItem(
                    label = { Text(text = "设置") },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "设置"
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onSettingsClick()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                // Add more drawer items here if needed
            }
        }
    ) {
        // 主界面
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {Box(modifier = Modifier.offset(x = (-12).dp)) {
                        Text(
                            text = "Notes",
                            fontSize = 20.sp
                        )
                    }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "菜单"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "搜索"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = customColors.screenBackground
                    )
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
                    .background(customColors.screenBackground)
                    .padding(it)
            ) {
                // Search Bar 搜索栏 - 仅在搜索按钮被点击时显示
                if (isSearchVisible) {
                    TextField(
                        value = viewModel.searchQuery,
                        onValueChange = { query -> viewModel.searchQuery = query },
                        placeholder = {
                            Text(
                                text = "搜索笔记...",
                                style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                            )
                        },
                        // 输入框样式
                        modifier = Modifier
                            .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp
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
                }
                
                // Notes List 笔记列表
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    // 减小顶部间距
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                    // 多条笔记间隔
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotes) { note ->
                        NoteCard(
                            note = note,
                            onEditClick = { onEditNoteClick(note) },
                            onDeleteClick = { viewModel.deleteNote(it.id) }
                        )
                    }
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
fun NotesScreenPreview() {
    // Preview doesn't work with AndroidViewModel, so we show a placeholder
    Text("Notes Screen Preview")
}
