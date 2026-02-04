package com.example.journey.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.example.journey.ui.theme.LocalCustomColors
import com.example.journey.utils.AppSoundPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 添加笔记对话框
 * 使用原生 ModalBottomSheet 设计，白色背景
 * 初始高度为 5 行行高，点击 FAB 后自动弹出键盘
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onSaveNote: (String, List<String>) -> Unit,
    availableTags: List<String> = emptyList()
) {
    val editorState = rememberWysiwygEditorState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val customColors = LocalCustomColors.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // 标签相关状态
    var showTagSuggestions by remember { mutableStateOf(false) }
    var currentTagPrefix by remember { mutableStateOf("") }
    var cursorOffset by remember { mutableStateOf(androidx.compose.ui.unit.IntOffset(0, 0)) }
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

    // 计算 5 行行高的高度（每行 24sp 行高 + 间距）
    val fiveLineHeight = remember(density) {
        with(density) { (24.sp.toDp() * 5 + 32.dp) } // 5行 + 上下padding
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
                        cursorOffset = androidx.compose.ui.unit.IntOffset(
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

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { true }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color.White, // 白色背景
        dragHandle = { BottomSheetDefaults.DragHandle() },
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        // 使用 Box 作为根容器
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 60.dp) // 为发送按钮留出空间
            ) {
                // 编辑器区域 - 固定为5行行高
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fiveLineHeight)
                        .padding(horizontal = 16.dp)
                ) {
                    // 标签补全 Popup - 跟随光标位置
                    TagSuggestionsPopup(
                        visible = showTagSuggestions && filteredTags.isNotEmpty(),
                        tags = filteredTags,
                        cursorOffset = cursorOffset,
                        onTagSelected = { tag ->
                            insertTag(editorState, tag) { showTagSuggestions = false }
                        },
                        onDismiss = { showTagSuggestions = false }
                    )

                    WysiwygEditor(
                        state = editorState,
                        onValueChange = { checkIsTypingTag() },
                        focusRequester = focusRequester,
                        placeholder = "现在的想法是...",
                        modifier = Modifier.fillMaxSize(),
                        onTextLayout = { layoutResult ->
                            textLayoutResult = layoutResult
                        }
                    )
                }
            }

            // 发送按钮 - 绝对定位在底部，紧贴键盘
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .windowInsetsPadding(WindowInsets.ime) // 紧贴键盘
                    .navigationBarsPadding(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = {
                        val content = editorState.textFieldValue.text
                        if (content.isNotBlank()) {
                            // 播放发送音效（使用全局音效器）
                            AppSoundPlayer.play()
                            // 触发震动反馈
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            
                            val extractedTags = extractTags(content)
                            val contentWithoutTags = removeTags(content)
                            onSaveNote(contentWithoutTags, extractedTags)
                            // 直接关闭弹窗，音效器在全局不受弹窗生命周期影响
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.markdownLink,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "保存笔记",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "发送",
                        style = TextStyle(fontSize = 14.sp)
                    )
                }
            }
        }
    }

    // 自动获取焦点并弹出键盘
    LaunchedEffect(Unit) {
        delay(100) // 等待弹窗动画开始
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
    onTagSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val customColors = LocalCustomColors.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    // 计算 Popup 显示位置
    val (popupOffset, expandFrom) = remember(cursorOffset, tags.size) {
        with(density) {
            // 行高（24sp）
            val lineHeight = 24.sp.toDp().toPx().toInt()

            // 估算列表高度（每个标签项约 48dp + 分割线）
            val estimatedItemHeight = 48.dp.toPx().toInt()
            val estimatedListHeight = (tags.size * estimatedItemHeight).coerceIn(100, 280.dp.toPx().toInt())

            // 编辑器高度（5行）
            val editorHeight = (24.sp.toDp() * 5 + 32.dp).toPx().toInt()

            // 光标下方可用空间
            val spaceBelow = editorHeight - cursorOffset.y
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
