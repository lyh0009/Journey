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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.journey.ui.theme.LocalCustomColors

/**
 * WYSIWYG 编辑器组件
 * 所见即所得模式：使用 VisualTransformation 实时渲染 Markdown 样式
 */
@Composable
fun WysiwygEditor(
    state: WysiwygEditorState,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    placeholder: String = "现在的想法是...",
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val customColors = LocalCustomColors.current

    // 创建 Markdown 视觉转换器
    val markdownTransformation = remember(customColors) {
        MarkdownVisualTransformation(customColors)
    }

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
            color = customColors.markdownBody,
            lineHeight = 24.sp
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        ),
        maxLines = Int.MAX_VALUE,
        visualTransformation = markdownTransformation,
        onTextLayout = onTextLayout,
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                // 占位符
                if (state.textFieldValue.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = customColors.markdownHint,
                            lineHeight = 24.sp
                        )
                    )
                }
                // 实际输入框（带有 Markdown 视觉转换）
                innerTextField()
            }
        }
    )
}
