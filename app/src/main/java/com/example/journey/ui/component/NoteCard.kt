package com.example.journey.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.journey.data.Note
import com.example.journey.ui.theme.LocalCustomColors


@Composable
fun NoteCard(note: Note) {
    var isExpanded by remember { mutableStateOf(false) }
    var isOverFlowed by remember { mutableStateOf(false) }
    val customColors = LocalCustomColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { isExpanded = !isExpanded }
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = customColors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 时间戳
            Text(
                text = note.formattedDate,
                style = TextStyle(
                    fontSize = 14.sp, 
                    color = customColors.markdownHint
                )
            )
            
            // 标签显示区域
            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(note.tags) { tag ->
                        TagChip(tag = tag)
                    }
                }
            }

            // 正文内容：使用 Markdown 渲染
            MarkdownRenderer(
                content = note.content,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                maxLines = if (isExpanded) Int.MAX_VALUE else 5,
                onTextLayout = { textLayoutResult ->
                    // 检测是否溢出：当实际行数超过5行或文本被截断时
                    if (!isExpanded) {
                        isOverFlowed = textLayoutResult.lineCount >= 5 || textLayoutResult.hasVisualOverflow
                    }
                }
            )

            // 只有检测到溢出时才显示展开/收起按钮
            if (isOverFlowed) {
                Text(
                    text = if (isExpanded) "收起" else "展开",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = customColors.markdownLink,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isExpanded = !isExpanded }
                        )
                )
            }
        }
    }
}

@Composable
fun TagChip(tag: String) {
    val customColors = LocalCustomColors.current
    
    Box(
        modifier = Modifier
            .background(
                color = customColors.markdownCodeBackground,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "#$tag",
            style = TextStyle(
                fontSize = 14.sp,
                color = customColors.markdownLink,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun NoteCardPreview() {
    val sampleNote = Note(
        content = """这是一条示例笔记，用于预览NoteCard组件的效果。
            
## 支持 Markdown

- **加粗文本**
- *斜体文本*
- ~~删除线~~
- ==高亮文本==
- `行内代码`

> 这是一段引用

[链接](https://example.com)""".trimIndent(),
        tags = listOf("开心", "工作", "重要")
    )
    NoteCard(note = sampleNote)
}

@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun NoteCardPreviewWithLongText() {
    val sampleNote = Note(
        content = """这是一条很长的笔记，用于测试文本截断和展开功能。

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.

Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.

Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.

Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.""".trimIndent(),
        tags = listOf("测试")
    )
    NoteCard(note = sampleNote)
}
