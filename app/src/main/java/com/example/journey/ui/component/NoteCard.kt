package com.example.journey.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.journey.data.Note


@Composable
fun NoteCard(note: Note) {
    var isExpanded by remember { mutableStateOf(false) }
    var isOverFlowed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // 去掉点击反馈
                onClick = { isExpanded = !isExpanded }
            ),
        // 卡片圆角
        shape = RoundedCornerShape(20.dp),
        // 卡片阴影
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 时间戳
            Text(
                text = note.formattedDate,
                style = TextStyle(fontSize = 14.sp, color = Color.Gray)
            )

            // 正文内容：直接在这里检测行数
            Text(
                text = note.content,
                style = TextStyle(fontSize = 16.sp),
                maxLines = if (isExpanded) Int.MAX_VALUE else 6,
                overflow = TextOverflow.Clip, // 截断
                onTextLayout = { textLayoutResult ->
                    // 关键逻辑：
                    // didOverflowHeight 表示内容是否因为 maxLines 限制而溢出了
                    // 或者判断 lineCount 是否大于我们设定的阈值
                    if (!isExpanded) { // 只在收起状态下检测，避免展开后逻辑冲突
                        isOverFlowed = textLayoutResult.didOverflowHeight || textLayoutResult.lineCount > 6
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // 只有检测到溢出时才显示按钮
            if (isOverFlowed) {
                Text(
                    text = if (isExpanded) "收起" else "展开",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF5882FA),
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

@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun NoteCardPreview() {
    val sampleNote = Note(
        content = "这是一条示例笔记，用于预览NoteCard组件的效果。这条笔记包含了一些文本内容，以便测试文本截断和展开功能。",
        createdAt = java.time.LocalDateTime.now()
    )
    NoteCard(note = sampleNote)
}