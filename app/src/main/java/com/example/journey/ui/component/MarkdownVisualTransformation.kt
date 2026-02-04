package com.example.journey.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.journey.ui.theme.MarkdownCustomColors

/**
 * Markdown 视觉转换器
 * 将 Markdown 源码实时转换为带样式的文本
 * 保留 Markdown 符号但使用不同颜色显示
 */
class MarkdownVisualTransformation(
    private val colors: MarkdownCustomColors
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val transformed = parseMarkdown(text.text, colors)
        return TransformedText(transformed, OffsetMapping.Identity)
    }

    companion object {
        fun parseMarkdown(text: String, colors: MarkdownCustomColors): AnnotatedString {
            val textLength = text.length
            return buildAnnotatedString {
                append(text)

                // 1. 代码块 ```code```
                val codeBlockRegex = Regex("```[\\s\\S]*?```")
                codeBlockRegex.findAll(text).forEach { match ->
                    val start = match.range.first.coerceIn(0, textLength)
                    val end = (match.range.last + 1).coerceIn(0, textLength)
                    if (start < end) {
                        addStyle(
                            SpanStyle(
                                background = colors.markdownCodeBackground,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            ),
                            start,
                            end
                        )
                    }
                }

                // 2. 行内代码 `code`
                val inlineCodeRegex = Regex("`([^`]+)`")
                inlineCodeRegex.findAll(text).forEach { match ->
                    val start = match.range.first.coerceIn(0, textLength)
                    val end = (match.range.last + 1).coerceIn(0, textLength)
                    val contentStart = (start + 1).coerceIn(0, textLength)
                    val contentEnd = match.range.last.coerceIn(0, textLength)

                    if (start < contentStart) {
                        addStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.5f)), start, contentStart)
                    }
                    if (contentEnd < end) {
                        addStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.5f)), contentEnd, end)
                    }
                    if (contentStart < contentEnd) {
                        addStyle(
                            SpanStyle(
                                background = colors.markdownCodeBackground,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = colors.markdownCodeText
                            ),
                            contentStart,
                            contentEnd
                        )
                    }
                }

                // 3. 加粗 **text** 或 __text__
                val boldRegex = Regex("(\\*\\*|__)(.*?)(\\*\\*|__)")
                boldRegex.findAll(text).forEach { match ->
                    val groups = match.groupValues
                    val prefix = groups[1]
                    val content = groups[2]

                    val start = match.range.first.coerceIn(0, textLength)
                    val end = (match.range.last + 1).coerceIn(0, textLength)
                    val contentStart = (start + prefix.length).coerceIn(0, textLength)
                    val contentEnd = (contentStart + content.length).coerceIn(0, textLength)

                    if (start < contentStart) {
                        addStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.5f)), start, contentStart)
                    }
                    if (contentEnd < end) {
                        addStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.5f)), contentEnd, end)
                    }
                    if (contentStart < contentEnd) {
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold), contentStart, contentEnd)
                    }
                }

                // 4. 斜体 *text* 或 _text_ (排除 ** 的情况)
                val italicRegex = Regex("(?<!\\*)\\*(?!\\*)([^*]+)(?<!\\*)\\*(?!\\*)")
                italicRegex.findAll(text).forEach { match ->
                    val start = match.range.first.coerceIn(0, textLength)
                    val end = (match.range.last + 1).coerceIn(0, textLength)
                    val contentStart = (start + 1).coerceIn(0, textLength)
                    val contentEnd = match.range.last.coerceIn(0, textLength)

                    if (start < contentStart) {
                        addStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.5f)), start, contentStart)
                    }
                    if (contentEnd < end) {
                        addStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.5f)), contentEnd, end)
                    }
                    if (contentStart < contentEnd) {
                        addStyle(
                            SpanStyle(fontStyle = FontStyle.Italic),
                            contentStart,
                            contentEnd
                        )
                    }
                }

                // 5. 删除线 ~~text~~
                val strikethroughRegex = Regex("~~(.*?)~~")
                strikethroughRegex.findAll(text).forEach { match ->
                    val groups = match.groupValues
                    val content = groups[1]

                    val start = match.range.first.coerceIn(0, textLength)
                    val end = (match.range.last + 1).coerceIn(0, textLength)
                    val contentStart = (start + 2).coerceIn(0, textLength)
                    val contentEnd = (contentStart + content.length).coerceIn(0, textLength)

                    if (start < contentStart) {
                        addStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.5f)), start, contentStart)
                    }
                    if (contentEnd < end) {
                        addStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.5f)), contentEnd, end)
                    }
                    if (contentStart < contentEnd) {
                        addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), contentStart, contentEnd)
                    }
                }

                // 6. 高亮 ==text==
                val highlightRegex = Regex("==(.*?)==")
                highlightRegex.findAll(text).forEach { match ->
                    val groups = match.groupValues
                    val content = groups[1]

                    val start = match.range.first.coerceIn(0, textLength)
                    val end = (match.range.last + 1).coerceIn(0, textLength)
                    val contentStart = (start + 2).coerceIn(0, textLength)
                    val contentEnd = (contentStart + content.length).coerceIn(0, textLength)

                    if (start < contentStart) {
                        addStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.5f)), start, contentStart)
                    }
                    if (contentEnd < end) {
                        addStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.5f)), contentEnd, end)
                    }
                    if (contentStart < contentEnd) {
                        addStyle(
                            SpanStyle(background = Color(0xFFFFFF00).copy(alpha = 0.3f)),
                            contentStart,
                            contentEnd
                        )
                    }
                }

                // 7. 标签 #tag (支持中文、英文、数字、下划线)
                val tagRegex = Regex("#([\\w\\u4e00-\\u9fa5]+)")
                tagRegex.findAll(text).forEach { match ->
                    val start = match.range.first.coerceIn(0, textLength)
                    val end = (match.range.last + 1).coerceIn(0, textLength)
                    if (start < end) {
                        addStyle(
                            SpanStyle(
                                color = colors.markdownLink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp // 明确设置字体大小，避免继承其他样式
                            ),
                            start,
                            end
                        )
                    }
                }

                // 8. 链接 [text](url)
                val linkRegex = Regex("\\[([^\\]]+)\\]\\(([^\\)]+)\\)")
                linkRegex.findAll(text).forEach { match ->
                    val groups = match.groupValues
                    val linkText = groups[1]
                    val url = groups[2]

                    val start = match.range.first.coerceIn(0, textLength)
                    val end = (match.range.last + 1).coerceIn(0, textLength)
                    val textStart = (start + 1).coerceIn(0, textLength)
                    val textEnd = (textStart + linkText.length).coerceIn(0, textLength)
                    val urlStart = (textEnd + 2).coerceIn(0, textLength)
                    val urlEnd = (urlStart + url.length).coerceIn(0, textLength)

                    // [ 和 ]
                    if (start < textStart) {
                        addStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.5f)), start, textStart)
                    }
                    if (textEnd < (textEnd + 1).coerceIn(0, textLength)) {
                        addStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.5f)), textEnd, textEnd + 1)
                    }
                    // 链接文本
                    if (textStart < textEnd) {
                        addStyle(SpanStyle(color = colors.markdownLink, textDecoration = TextDecoration.Underline), textStart, textEnd)
                    }
                    // ( 和 )
                    if ((textEnd + 1) < urlStart) {
                        addStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.5f)), textEnd + 1, urlStart)
                    }
                    if (urlEnd < end) {
                        addStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.5f)), urlEnd, end)
                    }
                    // URL
                    if (urlStart < urlEnd) {
                        addStyle(SpanStyle(color = Color.Gray, fontStyle = FontStyle.Italic), urlStart, urlEnd)
                    }
                }

                // 9. 引用 >
                val quoteRegex = Regex("^>\\s*(.+)$", RegexOption.MULTILINE)
                quoteRegex.findAll(text).forEach { match ->
                    val start = match.range.first.coerceIn(0, textLength)
                    val end = (match.range.last + 1).coerceIn(0, textLength)
                    val contentStart = (start + 1).coerceIn(0, textLength)

                    if (start < contentStart) {
                        addStyle(
                            SpanStyle(color = Color.Gray.copy(alpha = 0.5f)),
                            start,
                            contentStart
                        )
                    }
                    if (contentStart < end) {
                        addStyle(
                            SpanStyle(color = colors.markdownQuote),
                            contentStart,
                            end
                        )
                    }
                }

                // 10. 标题 # ## ### (必须后跟空格，避免与标签冲突)
                val headerRegex = Regex("^(#{1,6})\\s+(.+)$", RegexOption.MULTILINE)
                headerRegex.findAll(text).forEach { match ->
                    val hashes = match.groupValues[1]
                    val headerText = match.groupValues[2]

                    val start = match.range.first.coerceIn(0, textLength)
                    val end = (match.range.last + 1).coerceIn(0, textLength)
                    val hashEnd = (start + hashes.length).coerceIn(0, textLength)
                    val textStart = (hashEnd + 1).coerceIn(0, textLength)
                    val textEnd = (textStart + headerText.length).coerceIn(0, textLength)

                    // # 符号使用灰色
                    if (start < hashEnd) {
                        addStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.5f)), start, hashEnd)
                    }
                    // 标题文字使用粗体和大字号
                    if (textStart < textEnd) {
                        val level = hashes.length.coerceIn(1, 6)
                        val fontSize = when (level) {
                            1 -> 24.sp
                            2 -> 22.sp
                            3 -> 20.sp
                            4 -> 18.sp
                            5 -> 17.sp
                            else -> 16.sp
                        }
                        addStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = fontSize
                            ),
                            textStart,
                            textEnd
                        )
                    }
                }

                // 11. 无序列表 - 或 *
                val unorderedListRegex = Regex("^([\\s]*)([-*])\\s+(.+)$", RegexOption.MULTILINE)
                unorderedListRegex.findAll(text).forEach { match ->
                    val indent = match.groupValues[1]

                    val start = match.range.first.coerceIn(0, textLength)
                    val markerStart = (start + indent.length).coerceIn(0, textLength)
                    val markerEnd = (markerStart + 1).coerceIn(0, textLength)

                    // 列表标记符使用灰色
                    if (markerStart < markerEnd) {
                        addStyle(SpanStyle(color = colors.markdownListMarker, fontWeight = FontWeight.Bold), markerStart, markerEnd)
                    }
                }

                // 12. 有序列表 1. 2. 等
                val orderedListRegex = Regex("^([\\s]*)(\\d+)\\.\\s+(.+)$", RegexOption.MULTILINE)
                orderedListRegex.findAll(text).forEach { match ->
                    val indent = match.groupValues[1]
                    val number = match.groupValues[2]

                    val start = match.range.first.coerceIn(0, textLength)
                    val numberStart = (start + indent.length).coerceIn(0, textLength)
                    val numberEnd = (numberStart + number.length + 1).coerceIn(0, textLength)

                    // 数字和点使用灰色
                    if (numberStart < numberEnd) {
                        addStyle(SpanStyle(color = colors.markdownListMarker), numberStart, numberEnd)
                    }
                }
            }
        }
    }
}
