package com.example.journey.ui.screen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
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
import com.example.journey.ui.component.NoteCard
import com.example.journey.ui.theme.JourneyTheme
import com.example.journey.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NoteViewModel,
    onAddNoteClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val filteredNotes = viewModel.getFilteredNotes()
    var isSearchVisible by remember { mutableStateOf(false) }
    
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
                    text = "菜单",
                    modifier = Modifier.padding(16.dp),
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Drawer items
                NavigationDrawerItem(
                    label = { Text(text = "设置") },
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
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {Box(modifier = Modifier.offset(x = (-12).dp)) { // 使用负值减小间距，正值增大间距
                        Text(
                            text = "Notes",
                            fontSize = 20.sp
                        )}
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
                            start = 16.dp,// 减小左边间距，让它离屏幕更近一些
                            end = 16.dp,// 减小右边间距，让它离屏幕更近一些
                            top = 8.dp,// 减小顶部间距，让它离屏幕更近一些
                            bottom = 8.dp // 减小底部间距，让它离列表更近一些
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
@Preview(showBackground = true, name = "导航栏预览")
@Composable
fun NavDrawerPreview() {
    // 关键点：将初始状态设置为 Open
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerShape = RectangleShape
            ) {
                Text("菜单", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                NavigationDrawerItem(
                    label = { Text("设置") },
                    selected = false,
                    onClick = { }
                )
            }
        }
    ) {
        // 主屏幕内容
        Scaffold { padding ->
            Column(modifier = Modifier.padding(padding)) {
                Text("这是主内容区")
            }
        }
    }
}

@Composable
@Suppress("ViewModelConstructorInComposable")
fun NotesScreenWithNavBarPreview() {
    // Preview with navigation bar visible
    val viewModel = NoteViewModel()
    
    JourneyTheme {
        NotesScreen(
            viewModel = viewModel,
            onAddNoteClick = {},
            onSettingsClick = {}
        )
    }
}