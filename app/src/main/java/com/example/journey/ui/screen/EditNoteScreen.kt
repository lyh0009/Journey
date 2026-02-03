package com.example.journey.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.journey.data.Note
import com.example.journey.ui.component.*
import com.example.journey.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay

/**
 * 编辑笔记页面
 * 独立的编辑页面，不是弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    note: Note,
    onBackClick: () -> Unit,
    onSaveNote: (String, List<String>) -> Unit
) {
    val editorState = rememberWysiwygEditorState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val customColors = LocalCustomColors.current

    // 初始化编辑器内容
    LaunchedEffect(note) {
        val contentWithTags = if (note.tags.isNotEmpty()) {
            note.tags.joinToString(" ") { "#$it" } + " " + note.content
        } else {
            note.content
        }
        editorState.updateTextFieldValue(
            TextFieldValue(
                text = contentWithTags,
                selection = TextRange(contentWithTags.length)
            )
        )
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑笔记") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 保存按钮
                    IconButton(
                        onClick = {
                            val content = editorState.textFieldValue.text
                            if (content.isNotBlank()) {
                                val extractedTags = extractTags(content)
                                val contentWithoutTags = removeTags(content)
                                onSaveNote(contentWithoutTags, extractedTags)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "保存"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
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
                    placeholder = "编辑笔记内容...",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // 自动获取焦点
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
        keyboardController?.show()
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
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 2.dp,
            color = Color(0xFFF5F5F5)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(tags) { tag ->
                    Text(
                        text = "#$tag",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = customColors.markdownBody,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTagSelected(tag) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
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

@Preview(showBackground = true)
@Composable
fun EditNoteScreenPreview() {
    val sampleNote = Note(
        content = "这是一条示例笔记",
        tags = listOf("示例", "测试")
    )
    EditNoteScreen(
        note = sampleNote,
        onBackClick = {},
        onSaveNote = { _, _ -> }
    )
}
