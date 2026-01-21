package com.example.journey.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight



// 移除VisualTransformation，因为当前Compose版本的BasicTextField可能不支持它
// 我们将在后续使用其他方式实现标签高亮
/*
class TagVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val builder = AnnotatedString.Builder()
        val tagColor = Color(0xFF64B5F6)
        val tagStyle = SpanStyle(color = tagColor, fontWeight = FontWeight.Medium)
        
        // 逐字符处理，识别标签
        var i = 0
        
        while (i < originalText.length) {
            val currentChar = originalText[i]
            
            if (currentChar == '#') {
                // 开始一个标签
                val tagStart = i
                builder.append(currentChar)
                i++
                
                // 继续添加标签字符，直到遇到空格或结束
                while (i < originalText.length && !originalText[i].isWhitespace()) {
                    builder.append(originalText[i])
                    i++
                }
                
                // 添加标签样式
                val tagEnd = i
                builder.addStyle(tagStyle, tagStart, tagEnd)
            } else {
                // 普通文本
                builder.append(currentChar)
                i++
            }
        }
        
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onSaveNote: (String) -> Unit
) {
    // 使用TextFieldValue来管理文本内容、光标位置和选择范围
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    
    // 标签补全列表
    val tags = remember {
        mutableStateListOf("开心", "工作", "生活", "学习", "重要", "灵感")
    }
    
    // 显示标签补全列表的状态
    var showTagSuggestions by remember {
        mutableStateOf(false)
    }
    
    // 当前输入的标签前缀
    var currentTagPrefix by remember {
        mutableStateOf("")
    }
    
    val focusRequester = remember {
        FocusRequester()
    }
    
    // 实现插入标签功能的逻辑
    fun insertTag() {
        val currentValue = textFieldValue
        val selection = currentValue.selection
        val text = currentValue.text
        
        // 如果当前光标前不是空格，先补个空格
        val beforeCursor = if (selection.start > 0) text[selection.start - 1] else ' '
        val spaceNeeded = beforeCursor != ' '
        
        val newText = buildString {
            append(text.substring(0, selection.start))
            if (spaceNeeded) append(" ")
            append("#")
            append(text.substring(selection.end))
        }
        
        val newCursorPosition = selection.start + (if (spaceNeeded) 2 else 1)
        textFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPosition)
        )
        
        // 显示标签补全列表
        showTagSuggestions = true
        currentTagPrefix = ""
    }
    
    // 检测是否正在输入标签
    fun checkIsTypingTag() {
        val text = textFieldValue.text
        val cursorPosition = textFieldValue.selection.start
        
        if (cursorPosition > 0) {
            val charBefore = text[cursorPosition - 1]
            if (charBefore == '#') {
                // 刚刚输入了#
                showTagSuggestions = true
                currentTagPrefix = ""
            } else {
                // 检查是否在#之后
                val textBeforeCursor = text.substring(0, cursorPosition)
                val lastHashIndex = textBeforeCursor.lastIndexOf('#')
                if (lastHashIndex != -1) {
                    // 检查#之后是否有空格
                    val textAfterHash = textBeforeCursor.substring(lastHashIndex + 1)
                    if (textAfterHash.none { it.isWhitespace() }) {
                        // 正在输入标签
                        showTagSuggestions = true
                        currentTagPrefix = textAfterHash
                    } else {
                        // 不在输入标签
                        showTagSuggestions = false
                        currentTagPrefix = ""
                    }
                } else {
                    // 没有#
                    showTagSuggestions = false
                    currentTagPrefix = ""
                }
            }
        } else {
            showTagSuggestions = false
            currentTagPrefix = ""
        }
    }
    
    // 过滤标签补全列表
    val filteredTags = remember(currentTagPrefix) {
        if (currentTagPrefix.isEmpty()) {
            tags
        } else {
            tags.filter { it.startsWith(currentTagPrefix, ignoreCase = true) }
        }
    }

    // Create a state for the bottom sheet
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { false } // 禁用下滑关闭功能
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
            val scrollState = rememberScrollState()
            
            // 监听内容变化，自动滚动到底部
            LaunchedEffect(textFieldValue.text) {
                scrollState.scrollTo(scrollState.maxValue)
            }
            
            // 标签补全列表
            if (showTagSuggestions && filteredTags.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 2.dp,
                    color = Color.White
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(filteredTags) {
                            Text(
                                text = "#$it",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .clickable {
                                        // 插入选中的标签
                                        val currentValue = textFieldValue
                                        val text = currentValue.text
                                        val cursorPosition = currentValue.selection.start
                                        val textBeforeCursor = text.substring(0, cursorPosition)
                                        val lastHashIndex = textBeforeCursor.lastIndexOf('#')
                                        
                                        if (lastHashIndex != -1) {
                                            val newText = buildString {
                                                append(text.substring(0, lastHashIndex))
                                                append("#$it")
                                                append(text.substring(cursorPosition))
                                            }
                                            val newCursorPosition = lastHashIndex + it.length + 1
                                            textFieldValue = TextFieldValue(
                                                text = newText,
                                                selection = TextRange(newCursorPosition)
                                            )
                                        }
                                        
                                        showTagSuggestions = false
                                        currentTagPrefix = ""
                                    }
                            )
                        }
                    }
                }
            }
            
            BasicTextField(
                value = textFieldValue,
                onValueChange = { 
                    textFieldValue = it 
                    checkIsTypingTag()
                },
                // 1. 添加 cursorBrush 参数来设置光标颜色
                cursorBrush = SolidColor(Color(0xFF64B5F6)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = (5*24).dp, max = (18*24).dp) // 5行文字高度（24.sp行高 * 5）
                    .padding(horizontal = 16.dp)
                    .focusRequester(focusRequester)
                    .verticalScroll(scrollState), // 添加垂直滚动
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Black,
                    lineHeight = 24.sp
                ),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = "现在的想法是...",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            )
                        }
                        innerTextField()
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
                        IconButton(onClick = { insertTag() }) {
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
                            if (textFieldValue.text.isNotBlank()) {
                                onSaveNote(textFieldValue.text)
                                textFieldValue = TextFieldValue("")
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