package com.example.journey.ui.component

import android.media.SoundPool
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.journey.R
import com.example.journey.ui.theme.LocalCustomColors
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // SoundPool 音效 - 使用系统音效流类型
    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }
    var soundId by remember { mutableStateOf(0) }
    var isSoundLoaded by remember { mutableStateOf(false) }

    // 加载音效 - 使用转换后的文件
    LaunchedEffect(Unit) {
        soundId = soundPool.load(context, R.raw.seed_click_sound, 1)
        Log.d("AddNoteDialog", "Loading sound, soundId: $soundId")
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            Log.d("AddNoteDialog", "Sound loaded, sampleId: $sampleId, status: $status")
            if (sampleId == soundId && status == 0) {
                isSoundLoaded = true
                Log.d("AddNoteDialog", "Sound loaded successfully")
            } else {
                Log.e("AddNoteDialog", "Sound failed to load, status: $status")
            }
        }
    }

    // 注意：不在此处释放 SoundPool，因为弹窗关闭时音效可能还在播放
    // SoundPool 会在应用进程结束时自动释放

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
                            // 播放发送音效
                            if (isSoundLoaded && soundId != 0) {
                                val playId = soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                                Log.d("AddNoteDialog", "Playing sound, playId: $playId")
                            } else {
                                Log.d("AddNoteDialog", "Sound not loaded yet, isSoundLoaded: $isSoundLoaded, soundId: $soundId")
                            }
                            val extractedTags = extractTags(content)
                            val contentWithoutTags = removeTags(content)
                            onSaveNote(contentWithoutTags, extractedTags)
                            // 延迟关闭弹窗，让音效有时间播放
                            scope.launch {
                                delay(700)
                                onDismiss()
                            }
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
 * 跟随光标位置动态显示，不遮挡光标
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

    // 计算 Popup 位置：在光标上方显示
    // 如果光标在顶部（y < 280），则在下方显示
    val popupYOffset = remember(cursorOffset) {
        with(density) {
            val listHeight = 280.dp.toPx().toInt()
            if (cursorOffset.y < listHeight + 50) {
                // 光标在顶部，向下显示
                cursorOffset.y + 50
            } else {
                // 光标在下方，向上显示
                cursorOffset.y - listHeight - 20
            }
        }
    }

    if (visible) {
        Popup(
            alignment = Alignment.TopStart,
            offset = IntOffset(cursorOffset.x, popupYOffset),
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
                            expandFrom = Alignment.Bottom,
                            animationSpec = tween(250)
                        ),
                exit = fadeOut(animationSpec = tween(100)) +
                       shrinkVertically(
                           shrinkTowards = Alignment.Bottom,
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
