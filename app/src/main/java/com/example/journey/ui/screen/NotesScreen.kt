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
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.TextSnippet
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DataObject
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
import androidx.activity.compose.BackHandler
import com.example.journey.data.Note
import com.example.journey.ui.component.NoteCard
import com.example.journey.ui.theme.LocalCustomColors
import com.example.journey.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NoteViewModel,
    onAddNoteClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogsClick: () -> Unit = {},
    onEditNoteClick: (Note) -> Unit = {},
    onExport: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val filteredNotes = viewModel.getFilteredNotes()
    var isSearchVisible by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    val customColors = LocalCustomColors.current
    val context = androidx.compose.ui.platform.LocalContext.current

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

    // 处理返回手势：如果搜索框可见，先关闭搜索框
    BackHandler(enabled = isSearchVisible) {
        isSearchVisible = false
        viewModel.searchQuery = "" // 清空搜索内容
    }
    // 抽屉导航栏
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidthDp),
                drawerShape = RectangleShape,
                drawerContainerColor = customColors.screenBackground
            ) {
                // Drawer header
                Text(
                    text = "Notes",
                    modifier = Modifier.padding(16.dp),
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Drawer items
                // 导出功能
                NavigationDrawerItem(
                    label = { Text(text = "导出") },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = "导出"
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            showExportDialog = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                // settings
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

                // logs
                NavigationDrawerItem(
                    label = { Text(text = "日志") },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = "日志"
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onLogsClick()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
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
                        // 输入框样式 - 圆角矩形
                        modifier = Modifier
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            )
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.LightGray.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.LightGray.copy(alpha = 0.2f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        singleLine = true
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
                            onDeleteClick = { viewModel.deleteNote(it.id) },
                            onExport = onExport
                        )
                    }
                }
            }
        }

        // 导出对话框
        if (showExportDialog) {
            ExportDialog(
                notes = filteredNotes,
                onDismiss = { showExportDialog = false },
                onExport = { format, content ->
                    val dateStr = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    val (mimeType, fileName) = when (format) {
                        ExportFormat.MARKDOWN -> "text/markdown" to "Notes_$dateStr.md"
                        ExportFormat.JSON -> "application/json" to "Notes_$dateStr.json"
                        ExportFormat.TEXT -> "text/plain" to "Notes_$dateStr.txt"
                    }
                    onExport(content, mimeType, fileName)
                    showExportDialog = false
                }
            )
        }
    }
}

/**
 * 导出格式
 */
enum class ExportFormat {
    MARKDOWN, JSON, TEXT
}

/**
 * 导出对话框
 */
@Composable
fun ExportDialog(
    notes: List<Note>,
    onDismiss: () -> Unit,
    onExport: (ExportFormat, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导出笔记") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("选择导出格式：", style = TextStyle(fontSize = 14.sp))
                Spacer(modifier = Modifier.height(8.dp))

                // Markdown 格式
                ExportFormatItem(
                    icon = Icons.Rounded.Description,
                    title = "Markdown (.md)",
                    description = "导出为 Markdown 格式，保留标签和格式",
                    onClick = {
                        val content = exportToMarkdown(notes)
                        onExport(ExportFormat.MARKDOWN, content)
                    }
                )

                // JSON 格式
                ExportFormatItem(
                    icon = Icons.Rounded.DataObject,
                    title = "JSON (.json)",
                    description = "导出为 JSON 格式，包含完整数据",
                    onClick = {
                        val content = exportToJson(notes)
                        onExport(ExportFormat.JSON, content)
                    }
                )

                // TXT 格式
                ExportFormatItem(
                    icon = Icons.Rounded.TextSnippet,
                    title = "纯文本 (.txt)",
                    description = "导出为纯文本格式，简洁易读",
                    onClick = {
                        val content = exportToText(notes)
                        onExport(ExportFormat.TEXT, content)
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun ExportFormatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = description,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

/**
 * 导出为 Markdown 格式
 */
private fun exportToMarkdown(notes: List<Note>): String {
    val sb = StringBuilder()
    sb.appendLine("# 笔记导出")
    sb.appendLine()
    sb.appendLine("导出时间: ${java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
    sb.appendLine("笔记数量: ${notes.size}")
    sb.appendLine()
    sb.appendLine("---")
    sb.appendLine()

    notes.forEachIndexed { index, note ->
        sb.appendLine("## 笔记 ${index + 1}")
        sb.appendLine()
        sb.appendLine("**时间:** ${note.formattedDate}")
        if (note.tags.isNotEmpty()) {
            sb.appendLine("**标签:** ${note.tags.joinToString(", ") { "#$it" }}")
        }
        sb.appendLine()
        sb.appendLine(note.content)
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()
    }

    return sb.toString()
}

/**
 * 导出为 JSON 格式
 */
private fun exportToJson(notes: List<Note>): String {
    val gson = Gson()
    val exportData = mapOf(
        "exportTime" to java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        "count" to notes.size,
        "notes" to notes.map { note ->
            mapOf(
                "id" to note.id,
                "content" to note.content,
                "tags" to note.tags,
                "createdAt" to note.formattedDate
            )
        }
    )
    return gson.toJson(exportData)
}

/**
 * 导出为纯文本格式
 */
private fun exportToText(notes: List<Note>): String {
    val sb = StringBuilder()
    sb.appendLine("笔记导出")
    sb.appendLine("导出时间: ${java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
    sb.appendLine("笔记数量: ${notes.size}")
    sb.appendLine()
    sb.appendLine("=".repeat(50))
    sb.appendLine()

    notes.forEachIndexed { index, note ->
        sb.appendLine("[${index + 1}] ${note.formattedDate}")
        if (note.tags.isNotEmpty()) {
            sb.appendLine("标签: ${note.tags.joinToString(", ")}")
        }
        sb.appendLine()
        sb.appendLine(note.content)
        sb.appendLine()
        sb.appendLine("-".repeat(50))
        sb.appendLine()
    }

    return sb.toString()
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
