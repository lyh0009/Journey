package com.example.journey.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.journey.ui.theme.LocalCustomColors

/**
 * WYSIWYG 编辑器组件
 * 所见即所得模式：用户直接看到渲染后的样式
 */
@Composable
fun WysiwygEditor(
    state: WysiwygEditorState,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    placeholder: String = "现在的想法是..."
) {
    val scrollState = rememberScrollState()
    val customColors = LocalCustomColors.current

    // 自动滚动到底部
    LaunchedEffect(state.textFieldValue.text) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    BasicTextField(
        value = state.textFieldValue,
        onValueChange = {
            state.updateTextFieldValue(it)
            onValueChange(it)
        },
        cursorBrush = SolidColor(customColors.markdownLink),
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .verticalScroll(scrollState),
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = Color.Transparent,
            lineHeight = 24.sp
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        ),
        maxLines = Int.MAX_VALUE,
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                // 渲染带样式的文本（所见即所得）
                if (state.textFieldValue.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = customColors.markdownHint,
                            lineHeight = 24.sp
                        )
                    )
                } else {
                    RenderedMarkdownText(
                        text = state.textFieldValue.text,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 透明的实际输入框
                innerTextField()
            }
        }
    )
}
