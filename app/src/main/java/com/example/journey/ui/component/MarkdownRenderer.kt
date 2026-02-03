package com.example.journey.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.journey.ui.theme.LocalCustomColors

/**
 * Markdown 渲染器
 * 职责：将 Markdown 内容渲染为 Compose UI
 * 支持：段落、无序列表、有序列表、加粗、斜体、删除线、高亮、下划线、链接
 * 不支持：标题、行内代码、引用块、分隔线、表格
 */
@Composable
fun MarkdownRenderer(
    content: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    val customColors = LocalCustomColors.current
    val parser = remember { MarkdownParserImpl() }
    val elements = parser.parse(content)

    // 将所有内容合并为一个 AnnotatedString，以便正确应用 maxLines
    val annotatedString = remember(elements, customColors) {
        buildAnnotatedString {
            elements.forEachIndexed { index, element ->
                when (element) {
                    is MarkdownElement.Paragraph -> {
                        append(parseInlineToAnnotatedString(element.content, parser, customColors))
                    }
                    is MarkdownElement.UnorderedList -> {
                        element.items.forEach { item ->
                            append("• ")
                            append(parseInlineToAnnotatedString(item.content, parser, customColors))
                            append("\n")
                        }
                    }
                    is MarkdownElement.OrderedList -> {
                        element.items.forEach { (number, item) ->
                            append("$number. ")
                            append(parseInlineToAnnotatedString(item.content, parser, customColors))
                            append("\n")
                        }
                    }
                }
                // 段落之间添加空行
                if (index < elements.size - 1) {
                    append("\n")
                }
            }
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = customColors.markdownBody
        ),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = onTextLayout,
        modifier = modifier
    )
}

/**
 * 将行内 Markdown 解析为 AnnotatedString，正确处理换行符
 */
private fun parseInlineToAnnotatedString(
    text: String,
    parser: MarkdownParser,
    customColors: com.example.journey.ui.theme.MarkdownCustomColors
): AnnotatedString {
    // 按换行符分割，逐行解析
    val lines = text.split("\n")
    return buildAnnotatedString {
        lines.forEachIndexed { lineIndex, line ->
            if (lineIndex > 0) {
                append("\n") // 保留换行符
            }
            
            val tokens = parser.parseInline(line)
            tokens.forEach { token ->
                when (token) {
                    is MarkdownToken.Text -> append(token.content)
                    is MarkdownToken.Bold -> {
                        val start = this.length
                        append(token.content)
                        addStyle(MarkdownStyles.bold(), start, this.length)
                    }
                    is MarkdownToken.Italic -> {
                        val start = this.length
                        append(token.content)
                        addStyle(MarkdownStyles.italic(), start, this.length)
                    }
                    is MarkdownToken.Strikethrough -> {
                        val start = this.length
                        append(token.content)
                        addStyle(MarkdownStyles.strikethrough(), start, this.length)
                    }
                    is MarkdownToken.Highlight -> {
                        val start = this.length
                        append(token.content)
                        addStyle(
                            MarkdownStyles.highlight(customColors.markdownHighlight),
                            start,
                            this.length
                        )
                    }
                    is MarkdownToken.Underline -> {
                        val start = this.length
                        append(token.content)
                        addStyle(MarkdownStyles.underline(), start, this.length)
                    }
                    is MarkdownToken.Link -> {
                        val start = this.length
                        append(token.text)
                        addStyle(
                            MarkdownStyles.link(customColors.markdownLink),
                            start,
                            this.length
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MarkdownRendererPreview() {
    val sampleContent = """
        这是一条**加粗**的文本，还有*斜体*和~~删除线~~。
        
        这是==高亮==文本和<u>下划线</u>。
        
        - 无序列表项 1
        - 无序列表项 2
          - 嵌套项
        
        1. 有序列表项 1
        2. 有序列表项 2
        
        这是一个[链接](https://example.com)。
    """.trimIndent()

    MarkdownRenderer(
        content = sampleContent,
        modifier = Modifier.padding(16.dp)
    )
}
