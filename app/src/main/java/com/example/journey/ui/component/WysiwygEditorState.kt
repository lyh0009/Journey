package com.example.journey.ui.component

import androidx.compose.runtime.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * WYSIWYG 编辑器状态接口
 */
interface WysiwygEditorState {
    val textFieldValue: TextFieldValue
    fun updateTextFieldValue(value: TextFieldValue)
    fun toggleBold()
    fun toggleItalic()
    fun toggleStrikethrough()
    fun toggleHighlight()
    fun toggleUnderline()
    fun insertUnorderedList()
    fun insertOrderedList()
    fun insertLink()
    fun insertTag()
}

/**
 * WYSIWYG 编辑器状态实现
 */
class WysiwygEditorStateImpl(initialText: String = "") : WysiwygEditorState {
    private var _textFieldValue by mutableStateOf(
        TextFieldValue(
            text = initialText,
            selection = TextRange(initialText.length)
        )
    )

    override val textFieldValue: TextFieldValue get() = _textFieldValue

    override fun updateTextFieldValue(value: TextFieldValue) {
        _textFieldValue = value
    }

    override fun toggleBold() = wrapSelection("**", "**")
    override fun toggleItalic() = wrapSelection("*", "*")
    override fun toggleStrikethrough() = wrapSelection("~~", "~~")
    override fun toggleHighlight() = wrapSelection("==", "==")
    override fun toggleUnderline() = wrapSelection("<u>", "</u>")

    override fun insertUnorderedList() = insertAtLineStart("- ")
    override fun insertOrderedList() = insertAtLineStart("1. ")

    override fun insertLink() {
        val selection = _textFieldValue.selection
        if (selection.collapsed) {
            insertText("[链接文本](url)", selection.start)
        } else {
            val text = _textFieldValue.text
            val selectedText = text.substring(selection.start, selection.end)
            replaceSelection("[$selectedText](url)")
        }
    }

    override fun insertTag() {
        insertText("#", _textFieldValue.selection.start)
    }

    private fun wrapSelection(prefix: String, suffix: String) {
        val selection = _textFieldValue.selection
        val text = _textFieldValue.text

        val newText = if (selection.collapsed) {
            buildString {
                append(text.substring(0, selection.start))
                append(prefix)
                append(suffix)
                append(text.substring(selection.end))
            }
        } else {
            val selectedText = text.substring(selection.start, selection.end)
            buildString {
                append(text.substring(0, selection.start))
                append(prefix)
                append(selectedText)
                append(suffix)
                append(text.substring(selection.end))
            }
        }

        val newCursorPosition = if (selection.collapsed) {
            selection.start + prefix.length
        } else {
            selection.end + prefix.length + suffix.length
        }

        _textFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPosition)
        )
    }

    private fun insertText(insertText: String, position: Int) {
        val text = _textFieldValue.text
        val newText = buildString {
            append(text.substring(0, position))
            append(insertText)
            append(text.substring(position))
        }
        val newCursorPosition = position + insertText.length
        _textFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPosition)
        )
    }

    private fun replaceSelection(replacement: String) {
        val selection = _textFieldValue.selection
        val text = _textFieldValue.text
        val newText = buildString {
            append(text.substring(0, selection.start))
            append(replacement)
            append(text.substring(selection.end))
        }
        val newCursorPosition = selection.start + replacement.length
        _textFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPosition)
        )
    }

    private fun insertAtLineStart(prefix: String) {
        val selection = _textFieldValue.selection
        val text = _textFieldValue.text
        val cursorPosition = selection.start

        val lineStart = text.lastIndexOf('\n', cursorPosition - 1).let {
            if (it == -1) 0 else it + 1
        }

        val newText = buildString {
            append(text.substring(0, lineStart))
            append(prefix)
            append(text.substring(lineStart))
        }

        val newCursorPosition = cursorPosition + prefix.length
        _textFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPosition)
        )
    }
}

/**
 * 创建 WYSIWYG 编辑器状态的 Composable 函数
 */
@Composable
fun rememberWysiwygEditorState(initialText: String = ""): WysiwygEditorState {
    return remember { WysiwygEditorStateImpl(initialText) }
}
