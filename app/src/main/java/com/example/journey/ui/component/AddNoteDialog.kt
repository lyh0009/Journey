package com.example.journey.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.journey.ui.theme.LocalCustomColors

/**
 * 添加笔记对话框
 * 使用 WYSIWYG 编辑器实现所见即所得的 Markdown 编辑体验
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onSaveNote: (String, List<String>) -> Unit
) {
    val editorState = rememberWysiwygEditorState()
    val focusRequester = remember { FocusRequester() }
    val customColors = LocalCustomColors.current
    
    // 标签相关状态
    var showTagSuggestions by remember { mutableStateOf(false) }
    var currentTagPrefix by remember { mutableStateOf("") }
    val tags = remember { mutableStateListOf("开心", "工作", "生活", "学习", "重要", "灵感") }
    
    // 过滤标签
    val filteredTags = remember(currentTagPrefix) {
        if (currentTagPrefix.isEmpty()) tags
        else tags.filter { it.startsWith(currentTagPrefix, ignoreCase = true) }
    }
    
    // 检测标签输入
    fun checkIsTypingTag() {
        val text = editorState.textFieldValue.text
        val cursorPosition = editorState.textFieldValue.selection.start
        
        if (cursorPosition > 0) {
            val textBeforeCursor = text.substring(0, cursorPosition)
            val lastHashIndex = textBeforeCursor.lastIndexOf('#')
            
            if (lastHashIndex != -1) {
                val textAfterHash = textBeforeCursor.substring(lastHashIndex + 1)
                if (textAfterHash.none { it.isWhitespace() }) {
                    showTagSuggestions = true
                    currentTagPrefix = textAfterHash
                } else {
                    showTagSuggestions = false
                    currentTagPrefix = ""
                }
            } else {
                showTagSuggestions = false
                currentTagPrefix = ""
            }
        } else {
            showTagSuggestions = false
            currentTagPrefix = ""
        }
    }
    
    // 提取标签
    fun extractTags(text: String): List<String> {
        return Regex("#([\\w\\u4e00-\\u9fa5]+)").findAll(text)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
    }
    
    // 移除标签后的内容
    fun removeTags(text: String): String {
        return text.replace(Regex("#[\\w\\u4e00-\\u9fa5]+"), "").trim()
    }
    
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { false }
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // 顶部标题栏
            DialogHeader(
                title = "编辑笔记",
                onClose = onDismiss
            )
            
            // 标签补全列表
            TagSuggestions(
                visible = showTagSuggestions && filteredTags.isNotEmpty(),
                tags = filteredTags,
                onTagSelected = { tag ->
                    insertTag(editorState, tag) { showTagSuggestions = false }
                }
            )
            
            // 编辑器区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                WysiwygEditor(
                    state = editorState,
                    onValueChange = { checkIsTypingTag() },
                    focusRequester = focusRequester,
                    placeholder = "现在的想法是...\n\n支持 Markdown 语法：\n**粗体** *斜体* ~~删除线~~ ==高亮==",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // 底部工具栏和发送按钮
            DialogFooter(
                editorState = editorState,
                charCount = editorState.textFieldValue.text.length,
                onSend = {
                    val content = editorState.textFieldValue.text
                    if (content.isNotBlank()) {
                        val extractedTags = extractTags(content)
                        val contentWithoutTags = removeTags(content)
                        onSaveNote(contentWithoutTags, extractedTags)
                        onDismiss()
                    }
                }
            )
        }
    }
    
    // 自动获取焦点
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

/**
 * 对话框头部
 */
@Composable
private fun DialogHeader(
    title: String,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(onClick = onClose) {
                Text("关闭")
            }
        }
    }
}

/**
 * 标签建议列表
 */
@Composable
private fun TagSuggestions(
    visible: Boolean,
    tags: List<String>,
    onTagSelected: (String) -> Unit
) {
    val customColors = LocalCustomColors.current
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(tags) { tag ->
                    Text(
                        text = "#$tag",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = customColors.markdownBody
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .clickable { onTagSelected(tag) }
                    )
                }
            }
        }
    }
}

/**
 * 对话框底部
 */
@Composable
private fun DialogFooter(
    editorState: WysiwygEditorState,
    charCount: Int,
    onSend: () -> Unit
) {
    val customColors = LocalCustomColors.current
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp
    ) {
        Column {
            // Markdown 工具栏
            MarkdownToolbar(
                state = editorState,
                config = MarkdownToolbarConfig(
                    showBold = true,
                    showItalic = true,
                    showStrikethrough = true,
                    showHighlight = true,
                    showUnderline = true,
                    showUnorderedList = true,
                    showOrderedList = true,
                    showLink = true,
                    showTag = true
                )
            )
            
            // 发送按钮栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 字符统计
                Text(
                    text = "$charCount 字符",
                    style = MaterialTheme.typography.bodySmall,
                    color = customColors.markdownHint
                )
                
                // 发送按钮
                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(52.dp)
                        .height(32.dp)
                        .background(
                            color = customColors.markdownLink,
                            shape = RoundedCornerShape(50)
                        )
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "保存笔记",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * 插入标签
 */
private fun insertTag(
    state: WysiwygEditorState,
    tag: String,
    onComplete: () -> Unit
) {
    val currentValue = state.textFieldValue
    val text = currentValue.text
    val cursorPosition = currentValue.selection.start
    val textBeforeCursor = text.substring(0, cursorPosition)
    val lastHashIndex = textBeforeCursor.lastIndexOf('#')
    
    if (lastHashIndex != -1) {
        val newText = buildString {
            append(text.substring(0, lastHashIndex))
            append("#$tag ")
            append(text.substring(cursorPosition))
        }
        val newCursorPosition = lastHashIndex + tag.length + 2
        state.updateTextFieldValue(
            TextFieldValue(
                text = newText,
                selection = TextRange(newCursorPosition)
            )
        )
    }
    
    onComplete()
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
        onSaveNote = { _, _ -> }
    )
}
