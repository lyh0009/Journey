package com.example.journey.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.journey.ui.theme.LocalCustomColors

/**
 * 预览模式下的 Markdown 渲染
 * 移除所有 markdown 标记，只显示纯文本和样式
 */
@Composable
fun WysiwygPreview(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE
) {
    val customColors = LocalCustomColors.current
    val parser = remember { MarkdownParserImpl() }

    Column(modifier = modifier) {
        val elements = parser.parse(text)

        elements.forEachIndexed { index, element ->
            when (element) {
                is MarkdownElement.Paragraph -> {
                    Text(
                        text = buildInlineAnnotatedString(element.content, parser, customColors),
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = customColors.markdownBody,
                            lineHeight = 24.sp
                        ),
                        maxLines = maxLines,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                is MarkdownElement.UnorderedList -> {
                    element.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .padding(start = (item.indentLevel * 24).dp)
                        ) {
                            Text(
                                text = "•",
                                style = TextStyle(
                                    color = customColors.markdownListMarker,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                modifier = Modifier.width(24.dp)
                            )
                            Text(
                                text = buildInlineAnnotatedString(item.content, parser, customColors),
                                style = TextStyle(
                                    color = customColors.markdownBody,
                                    lineHeight = 24.sp,
                                    fontSize = 16.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                is MarkdownElement.OrderedList -> {
                    element.items.forEach { (number, item) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .padding(start = (item.indentLevel * 24).dp)
                        ) {
                            Text(
                                text = "$number.",
                                style = TextStyle(
                                    color = customColors.markdownListMarker,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                ),
                                modifier = Modifier.width(32.dp)
                            )
                            Text(
                                text = buildInlineAnnotatedString(item.content, parser, customColors),
                                style = TextStyle(
                                    color = customColors.markdownBody,
                                    lineHeight = 24.sp,
                                    fontSize = 16.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                is MarkdownElement.EmptyLine -> {
                    // 空行渲染为占位高度
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (index < elements.size - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * 构建行内 AnnotatedString
 */
@Composable
private fun buildInlineAnnotatedString(
    text: String,
    parser: MarkdownParser,
    customColors: com.example.journey.ui.theme.MarkdownCustomColors
): AnnotatedString {
    val tokens = parser.parseInline(text)
    val builder = AnnotatedString.Builder()

    tokens.forEach { token ->
        when (token) {
            is MarkdownToken.Text -> builder.append(token.content)
            is MarkdownToken.Bold -> {
                val start = builder.length
                builder.append(token.content)
                builder.addStyle(MarkdownStyles.bold(), start, builder.length)
            }
            is MarkdownToken.Italic -> {
                val start = builder.length
                builder.append(token.content)
                builder.addStyle(MarkdownStyles.italic(), start, builder.length)
            }
            is MarkdownToken.Strikethrough -> {
                val start = builder.length
                builder.append(token.content)
                builder.addStyle(MarkdownStyles.strikethrough(), start, builder.length)
            }
            is MarkdownToken.Highlight -> {
                val start = builder.length
                builder.append(token.content)
                builder.addStyle(MarkdownStyles.highlight(customColors.markdownHighlight), start, builder.length)
            }
            is MarkdownToken.Underline -> {
                val start = builder.length
                builder.append(token.content)
                builder.addStyle(MarkdownStyles.underline(), start, builder.length)
            }
            is MarkdownToken.Link -> {
                val start = builder.length
                builder.append(token.text)
                builder.addStyle(MarkdownStyles.link(customColors.markdownLink), start, builder.length)
            }
        }
    }

    return builder.toAnnotatedString()
}
