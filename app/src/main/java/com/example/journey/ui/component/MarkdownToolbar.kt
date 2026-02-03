package com.example.journey.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.journey.ui.theme.LocalCustomColors

/**
 * Markdown 工具栏配置
 */
data class MarkdownToolbarConfig(
    val showBold: Boolean = true,
    val showItalic: Boolean = true,
    val showStrikethrough: Boolean = true,
    val showHighlight: Boolean = true,
    val showUnderline: Boolean = true,
    val showUnorderedList: Boolean = true,
    val showOrderedList: Boolean = true,
    val showLink: Boolean = true,
    val showTag: Boolean = true
)

/**
 * Markdown 工具栏
 * 提供所见即所得编辑器的格式化按钮
 */
@Composable
fun MarkdownToolbar(
    state: WysiwygEditorState,
    config: MarkdownToolbarConfig = MarkdownToolbarConfig(),
    modifier: Modifier = Modifier
) {
    val customColors = LocalCustomColors.current
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 格式工具组
        if (config.showBold) {
            ToolbarButton(
                onClick = { state.toggleBold() },
                content = {
                    Text(
                        "B",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = customColors.markdownBody
                    )
                }
            )
        }
        
        if (config.showItalic) {
            ToolbarButton(
                onClick = { state.toggleItalic() },
                content = {
                    Text(
                        "I",
                        fontStyle = FontStyle.Italic,
                        fontSize = 18.sp,
                        color = customColors.markdownBody
                    )
                }
            )
        }
        
        if (config.showStrikethrough) {
            ToolbarButton(
                onClick = { state.toggleStrikethrough() },
                content = {
                    Text(
                        "S",
                        textDecoration = TextDecoration.LineThrough,
                        fontSize = 18.sp,
                        color = customColors.markdownBody
                    )
                }
            )
        }
        
        if (config.showHighlight) {
            ToolbarButton(
                onClick = { state.toggleHighlight() },
                content = {
                    Text(
                        "H",
                        modifier = Modifier.background(customColors.markdownHighlight),
                        fontSize = 16.sp,
                        color = customColors.markdownBody
                    )
                }
            )
        }
        
        if (config.showUnderline) {
            ToolbarButton(
                onClick = { state.toggleUnderline() },
                content = {
                    Text(
                        "U",
                        textDecoration = TextDecoration.Underline,
                        fontSize = 18.sp,
                        color = customColors.markdownBody
                    )
                }
            )
        }
        
        // 分隔线
        if (hasAnyFormatTool(config)) {
            ToolbarDivider()
        }
        
        // 列表工具组
        if (config.showUnorderedList) {
            ToolbarButton(
                onClick = { state.insertUnorderedList() },
                icon = Icons.AutoMirrored.Rounded.FormatListBulleted
            )
        }
        
        if (config.showOrderedList) {
            ToolbarButton(
                onClick = { state.insertOrderedList() },
                content = {
                    Text(
                        "1.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = customColors.markdownBody
                    )
                }
            )
        }
        
        // 分隔线
        if (config.showUnorderedList || config.showOrderedList) {
            ToolbarDivider()
        }
        
        // 其他工具
        if (config.showLink) {
            ToolbarButton(
                onClick = { state.insertLink() },
                icon = Icons.Rounded.Link
            )
        }
        
        if (config.showTag) {
            ToolbarButton(
                onClick = { state.insertTag() },
                icon = Icons.Rounded.Tag
            )
        }
    }
}

/**
 * 工具栏按钮
 */
@Composable
private fun ToolbarButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: (@Composable () -> Unit)? = null
) {
    val customColors = LocalCustomColors.current
    
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp)
    ) {
        if (content != null) {
            content()
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = customColors.markdownBody,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 工具栏分隔线
 */
@Composable
private fun ToolbarDivider() {
    val customColors = LocalCustomColors.current
    
    HorizontalDivider(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp),
        color = customColors.markdownDivider
    )
}

/**
 * 检查是否有任何格式工具被启用
 */
private fun hasAnyFormatTool(config: MarkdownToolbarConfig): Boolean {
    return config.showBold || 
           config.showItalic || 
           config.showStrikethrough || 
           config.showHighlight || 
           config.showUnderline
}
