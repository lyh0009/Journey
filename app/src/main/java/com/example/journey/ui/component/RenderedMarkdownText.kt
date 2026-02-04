package com.example.journey.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.journey.ui.theme.LocalCustomColors

/**
 * 渲染 Markdown 文本为所见即所得样式
 * 将 Markdown 标记转换为实际显示的样式
 */
@Composable
fun RenderedMarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val customColors = LocalCustomColors.current

    // 将文本按行分割，处理每一行
    val lines = text.lines()

    Column(modifier = modifier) {
        lines.forEachIndexed { index, line ->
            when {
                // 无序列表行
                line.matches(Regex("^\\s*[-*+]\\s+.*")) -> {
                    RenderedUnorderedListItem(line, customColors)
                }
                // 有序列表行
                line.matches(Regex("^\\s*\\d+\\.\\s+.*")) -> {
                    RenderedOrderedListItem(line, customColors)
                }
                // 普通文本行
                else -> {
                    RenderedInlineText(line, customColors)
                }
            }

            // 添加行间距（除了最后一行）
            if (index < lines.size - 1) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * 渲染无序列表项
 * 将 "- 内容" 渲染为 "• 内容"
 */
@Composable
private fun RenderedUnorderedListItem(line: String, customColors: com.example.journey.ui.theme.MarkdownCustomColors) {
    val matchResult = Regex("^(\\s*)([-*+])\\s+(.*)$").find(line)
    if (matchResult != null) {
        val indent = matchResult.groupValues[1].length
        val content = matchResult.groupValues[3]

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (indent * 8).dp)
        ) {
            // 列表标记 •
            Text(
                text = "•",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = customColors.markdownListMarker,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.width(20.dp)
            )
            // 内容（处理行内样式）
            Text(
                text = buildInlineAnnotatedString(content, customColors),
                style = TextStyle(
                    fontSize = 16.sp,
                    color = customColors.markdownBody,
                    lineHeight = 24.sp
                ),
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        RenderedInlineText(line, customColors)
    }
}

/**
 * 渲染有序列表项
 * 将 "1. 内容" 渲染为带数字的列表项
 */
@Composable
private fun RenderedOrderedListItem(line: String, customColors: com.example.journey.ui.theme.MarkdownCustomColors) {
    val matchResult = Regex("^(\\s*)(\\d+)\\.\\s+(.*)$").find(line)
    if (matchResult != null) {
        val indent = matchResult.groupValues[1].length
        val number = matchResult.groupValues[2]
        val content = matchResult.groupValues[3]

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (indent * 8).dp)
        ) {
            // 列表标记 1.
            Text(
                text = "$number.",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = customColors.markdownListMarker,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.width(28.dp)
            )
            // 内容（处理行内样式）
            Text(
                text = buildInlineAnnotatedString(content, customColors),
                style = TextStyle(
                    fontSize = 16.sp,
                    color = customColors.markdownBody,
                    lineHeight = 24.sp
                ),
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        RenderedInlineText(line, customColors)
    }
}

/**
 * 渲染普通行内文本
 */
@Composable
private fun RenderedInlineText(line: String, customColors: com.example.journey.ui.theme.MarkdownCustomColors) {
    // 空行渲染为占位符，保持行高一致
    if (line.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(24.dp))
    } else {
        Text(
            text = buildInlineAnnotatedString(line, customColors),
            style = TextStyle(
                fontSize = 16.sp,
                color = customColors.markdownBody,
                lineHeight = 24.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 构建行内 AnnotatedString
 * 处理加粗、斜体、删除线、高亮、下划线、链接、标签
 */
private fun buildInlineAnnotatedString(
    text: String,
    customColors: com.example.journey.ui.theme.MarkdownCustomColors
): AnnotatedString {
    return buildAnnotatedString {
        append(text)

        // 1. 删除线 ~~text~~
        Regex("~~([^~]+)~~").findAll(text).forEach { match ->
            addStyle(
                MarkdownStyles.strikethrough(),
                match.range.first,
                match.range.last + 1
            )
        }

        // 2. 高亮 ==text==
        Regex("==([^=]+)==").findAll(text).forEach { match ->
            addStyle(
                MarkdownStyles.highlight(customColors.markdownHighlight),
                match.range.first,
                match.range.last + 1
            )
        }

        // 3. 加粗 **text**
        Regex("\\*\\*([^*]+)\\*\\*").findAll(text).forEach { match ->
            addStyle(
                MarkdownStyles.bold(),
                match.range.first,
                match.range.last + 1
            )
        }

        // 4. 斜体 *text*（避免匹配加粗）
        Regex("(?<!\\*)\\*([^*]+)\\*(?!\\*)").findAll(text).forEach { match ->
            addStyle(
                MarkdownStyles.italic(),
                match.range.first,
                match.range.last + 1
            )
        }

        // 5. 下划线 <u>text</u>
        Regex("<u>([^<]+)</u>").findAll(text).forEach { match ->
            addStyle(
                MarkdownStyles.underline(),
                match.range.first,
                match.range.last + 1
            )
        }

        // 6. 链接 [text](url)
        Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)").findAll(text).forEach { match ->
            addStyle(
                MarkdownStyles.link(customColors.markdownLink),
                match.range.first,
                match.range.last + 1
            )
        }

        // 7. 标签 #tag
        Regex("#([\\w\\u4e00-\\u9fa5]+)").findAll(text).forEach { match ->
            addStyle(
                SpanStyle(color = customColors.markdownLink),
                match.range.first,
                match.range.last + 1
            )
        }
    }
}
