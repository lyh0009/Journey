package com.example.journey.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onSaveNote: (String) -> Unit
) {
    var content by remember {
        mutableStateOf("")
    }
    
    val focusRequester = remember {
        FocusRequester()
    }
    
    // Create a state for the bottom sheet
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = Color.White
    ) {
        // Bottom sheet content with proper padding for system bars
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {

            // Immersive Input Area 输入区
            BasicTextField(
                value = content,
                onValueChange = { content = it },
                // 1. 添加 cursorBrush 参数来设置光标颜色
                cursorBrush = SolidColor(Color(0xFF64B5F6)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp) // 5行文字高度（24.sp行高 * 5）
                    .padding(horizontal = 16.dp)
                    .focusRequester(focusRequester),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Black,
                    lineHeight = 24.sp
                ),
                decorationBox = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (content.isEmpty()) {
                            Text(
                                text = "现在的想法是...",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            )
                        }
                        it()
                    }
                },
                maxLines = Int.MAX_VALUE
            )
            
            // Toolbar - positioned above keyboard
            // 工具栏 - 去掉 shadowElevation
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Toolbar Items
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tag Button
                        IconButton(onClick = { content += " #" }) {
                            Icon(
                                imageVector = Icons.Rounded.Tag,
                                contentDescription = "插入标签",
                                tint = Color.DarkGray
                            )
                        }
                        
                        // Image Button
                        IconButton(onClick = { /* 选择本地图片 */ }) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "插入图片",
                                tint = Color.DarkGray
                            )
                        }
                        
                        // Bold Button
                        IconButton(onClick = { /* 加粗文本 */ }) {
                            Text(
                                text = "B",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            )
                        }
                        
                        // List Button
                        IconButton(onClick = { /* 插入列表 */ }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.FormatListBulleted,
                                contentDescription = "插入列表",
                                tint = Color.DarkGray
                            )
                        }
                        
                        // More Options Button
                        IconButton(onClick = { /* 更多选项 */ }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreHoriz,
                                contentDescription = "更多选项",
                                tint = Color.DarkGray
                            )
                        }
                    }
                    
                    // Right Send Button
                    IconButton(
                        onClick = {
                            if (content.isNotBlank()) {
                                onSaveNote(content)
                                content = ""
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .padding(end = 8.dp) // 增加一点右边距
                            // 1. 将背景改为胶囊形状 (CapsuleShape)
                            // 2. 这里的宽度比高度稍大一点，就能呈现出完美的椭圆胶囊感
                            .width(52.dp)
                            .height(32.dp)
                            .background(
                                color = Color(0xFF64B5F6),
                                shape = RoundedCornerShape(50))
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Send,
                            contentDescription = "保存笔记",
                            tint = Color(0xFFFFFFFF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Auto focus when dialog is shown
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun AddNoteDialogPreview() {
    AddNoteDialog(
        onDismiss = {},
        onSaveNote = {}
    )
}