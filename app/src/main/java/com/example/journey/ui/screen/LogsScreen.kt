package com.example.journey.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.journey.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay

/**
 * 日志显示页面
 * 用于显示应用日志
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    onBackClick: () -> Unit
) {
    val customColors = LocalCustomColors.current
    var logs by remember { mutableStateOf(listOf<String>()) }
    var isLoading by remember { mutableStateOf(true) }

    // 模拟加载日志数据
    LaunchedEffect(Unit) {
        delay(500)
        logs = listOf(
            "[INFO] 应用启动成功",
            "[INFO] 加载笔记数据完成",
            "[DEBUG] SoundPool 初始化成功",
            "[DEBUG] 音效加载成功",
            "[INFO] 用户打开日志页面"
        )
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日志") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = customColors.screenBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs) { log ->
                        LogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItem(log: String) {
    val customColors = LocalCustomColors.current

    // 根据日志级别设置颜色
    val color = when {
        log.contains("[ERROR]") -> androidx.compose.ui.graphics.Color.Red
        log.contains("[WARN]") -> androidx.compose.ui.graphics.Color(0xFFFFA000)
        log.contains("[DEBUG]") -> androidx.compose.ui.graphics.Color(0xFF2196F3)
        else -> customColors.markdownBody
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = customColors.cardBackground,
        tonalElevation = 1.dp
    ) {
        Text(
            text = log,
            style = TextStyle(
                fontSize = 14.sp,
                color = color,
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.padding(12.dp)
        )
    }
}
