package com.example.journey.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.journey.data.Note
import com.example.journey.ui.component.*
import com.example.journey.ui.theme.LocalCustomColors
import com.example.journey.utils.AppSoundPlayer
import kotlinx.coroutines.delay

/**
 * 编辑笔记页面
 * 独立的编辑页面，不是弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    note: Note,
    availableTags: List<String> = emptyList(),
    onBackClick: () -> Unit,
    onSaveNote: (String, List<String>) -> Unit
) {
    val editorState = rememberWysiwygEditorState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val customColors = LocalCustomColors.current
    val density = androidx.compose.ui.platform.LocalDensity.current

    // 初始化编辑器内容（只显示纯内容，标签在顶部标签栏显示）
    LaunchedEffect(note) {
        editorState.updateTextFieldValue(
            TextFieldValue(
                text = note.content,
                selection = TextRange(note.content.length)
            )
        )
    }

    // 标签相关状态
    var showTagSuggestions by remember { mutableStateOf(false) }
    var currentTagPrefix by remember { mutableStateOf("") }
    var cursorOffset by remember { mutableStateOf(IntOffset(0, 0)) }
    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }

    // 使用传入的可用标签
    val tags = remember(availableTags) {
        mutableStateListOf<String>().apply { addAll(availableTags) }
    }

    // 过滤标签
    val filteredTags = remember(currentTagPrefix, tags) {
        if (currentTagPrefix.isEmpty()) tags
        else tags.filter { it.startsWith(currentTagPrefix, ignoreCase = true) }
    }

    // 检测标签输入并计算光标位置
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
                    // 计算光标位置
                    textLayoutResult?.let { layoutResult ->
                        val cursorRect = layoutResult.getCursorRect(lastHashIndex)
                        cursorOffset = IntOffset(
                            x = cursorRect.left.toInt(),
                            y = cursorRect.top.toInt()
                        )
                    }
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
                title = { Text("") },
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
                                // 播放保存音效
                                AppSoundPlayer.play()
                                // 提取编辑器中的标签
                                val extractedTags = extractTags(content)
                                val contentWithoutTags = removeTags(content)
                                // 合并编辑器中的标签和原有标签（去重）
                                val allTags = (note.tags + extractedTags).distinct()
                                onSaveNote(contentWithoutTags, allTags)
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
                .padding(horizontal = 16.dp)
        ) {
            // 标签栏（有标签时显示）
            if (note.tags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp)
                ) {
                    items(note.tags) { tag ->
                        TagChip(tag = tag)
                    }
                }
            }

            // 时间戳
            Text(
                text = note.formattedDate,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = customColors.markdownHint
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 编辑器区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // 标签补全 Popup - 跟随光标位置
                TagSuggestionsPopup(
                    visible = showTagSuggestions && filteredTags.isNotEmpty(),
                    tags = filteredTags,
                    cursorOffset = cursorOffset,
                    density = density,
                    onTagSelected = { tag ->
                        insertTag(editorState, tag) { showTagSuggestions = false }
                    },
                    onDismiss = { showTagSuggestions = false }
                )

                WysiwygEditor(
                    state = editorState,
                    onValueChange = { checkIsTypingTag() },
                    focusRequester = focusRequester,
                    placeholder = "编辑笔记内容...",
                    modifier = Modifier.fillMaxSize(),
                    onTextLayout = { layoutResult ->
                        textLayoutResult = layoutResult
                    }
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
 * 标签建议 Popup
 * 智能选择显示位置：优先在光标下方，空间不足时在上方
 */
@Composable
private fun TagSuggestionsPopup(
    visible: Boolean,
    tags: List<String>,
    cursorOffset: IntOffset,
    density: androidx.compose.ui.unit.Density,
    onTagSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val customColors = LocalCustomColors.current
    val haptic = LocalHapticFeedback.current

    // 计算 Popup 显示位置
    val (popupOffset, expandFrom) = remember(cursorOffset, tags.size) {
        with(density) {
            // 行高（24sp）
            val lineHeight = 24.sp.roundToPx()

            // 估算列表高度（每个标签项约 48dp + 分割线）
            val estimatedItemHeight = 48.dp.toPx().toInt()
            val estimatedListHeight = (tags.size * estimatedItemHeight).coerceIn(100, 280.dp.toPx().toInt())

            // 获取父容器高度（使用屏幕高度作为参考）
            val screenHeight = 800.dp.toPx().toInt() // 估算值

            // 光标下方可用空间
            val spaceBelow = screenHeight - cursorOffset.y
            // 光标上方可用空间
            val spaceAbove = cursorOffset.y

            // 优先在下方显示，空间不足则在上方显示
            val showBelow = spaceBelow >= estimatedListHeight + lineHeight || spaceBelow >= spaceAbove

            val yOffset = if (showBelow) {
                // 在光标下方显示，距离一个行高
                cursorOffset.y + lineHeight
            } else {
                // 在光标上方显示，距离一个行高
                cursorOffset.y - estimatedListHeight - lineHeight
            }

            val alignment = if (showBelow) Alignment.TopStart else Alignment.BottomStart

            IntOffset(cursorOffset.x, yOffset) to alignment
        }
    }

    if (visible) {
        Popup(
            alignment = expandFrom,
            offset = popupOffset,
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(150)) +
                        expandVertically(
                            expandFrom = if (expandFrom == Alignment.TopStart) Alignment.Top else Alignment.Bottom,
                            animationSpec = tween(250)
                        ),
                exit = fadeOut(animationSpec = tween(100)) +
                       shrinkVertically(
                           shrinkTowards = if (expandFrom == Alignment.TopStart) Alignment.Top else Alignment.Bottom,
                           animationSpec = tween(150)
                       )
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(min = 200.dp, max = 300.dp)
                        .heightIn(max = 280.dp),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    LazyColumn {
                        items(tags) { tag ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptic.performHapticFeedback(
                                            HapticFeedbackType.TextHandleMove
                                        )
                                        onTagSelected(tag)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#",
                                    style = TextStyle(
                                        color = Color.Gray,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = tag,
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = customColors.markdownBody
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // 分割线
                            if (tag != tags.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = Color.LightGray.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
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
