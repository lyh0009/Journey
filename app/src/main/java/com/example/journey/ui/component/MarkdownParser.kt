package com.example.journey.ui.component

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/**
 * Markdown 解析器接口
 * 职责：将 Markdown 文本解析为结构化数据
 */
interface MarkdownParser {
    fun parse(content: String): List<MarkdownElement>
    fun parseInline(text: String): List<MarkdownToken>
}

/**
 * Markdown 块级元素
 */
sealed class MarkdownElement {
    abstract val rawText: String

    data class Paragraph(override val rawText: String, val content: String) : MarkdownElement()
    data class UnorderedList(override val rawText: String, val items: List<ListItem>) : MarkdownElement()
    data class OrderedList(override val rawText: String, val items: List<Pair<Int, ListItem>>) : MarkdownElement()
    data class EmptyLine(override val rawText: String) : MarkdownElement()
    data class Header(override val rawText: String, val level: Int, val content: String) : MarkdownElement()
}

/**
 * 列表项，支持嵌套
 */
data class ListItem(
    val content: String,
    val indentLevel: Int,
    val nestedItems: List<ListItem> = emptyList()
)

/**
 * Markdown 行内标记
 */
sealed class MarkdownToken {
    data class Text(val content: String) : MarkdownToken()
    data class Bold(val content: String) : MarkdownToken()
    data class Italic(val content: String) : MarkdownToken()
    data class Strikethrough(val content: String) : MarkdownToken()
    data class Highlight(val content: String) : MarkdownToken()
    data class Underline(val content: String) : MarkdownToken()
    data class Link(val text: String, val url: String) : MarkdownToken()
    data class Tag(val content: String) : MarkdownToken()
}

/**
 * Markdown 样式定义
 */
object MarkdownStyles {
    fun bold() = SpanStyle(fontWeight = FontWeight.Bold)
    fun italic() = SpanStyle(fontStyle = FontStyle.Italic)
    fun strikethrough() = SpanStyle(textDecoration = TextDecoration.LineThrough)
    fun underline() = SpanStyle(textDecoration = TextDecoration.Underline)
    fun highlight(backgroundColor: androidx.compose.ui.graphics.Color) = 
        SpanStyle(background = backgroundColor)
    fun link(color: androidx.compose.ui.graphics.Color) = 
        SpanStyle(color = color, textDecoration = TextDecoration.Underline)
}

/**
 * Markdown 解析器实现
 * 已完成：标题、段落、无序列表、有序列表、加粗、斜体、删除线、高亮、下划线、链接
 * 未完成：内代码、引用块、分隔线、表格
 */
class MarkdownParserImpl : MarkdownParser {
    
    override fun parse(content: String): List<MarkdownElement> {
        val elements = mutableListOf<MarkdownElement>()
        val lines = content.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]

            when {
                line.isBlank() -> {
                    // 添加空行元素
                    elements.add(MarkdownElement.EmptyLine(line))
                    i++
                }

                isHeaderLine(line) -> {
                    val (header, nextIndex) = parseHeader(lines, i)
                    elements.add(header)
                    i = nextIndex
                }

                isUnorderedListLine(line) -> {
                    val (items, nextIndex) = parseUnorderedList(lines, i)
                    elements.add(MarkdownElement.UnorderedList("", items))
                    i = nextIndex
                }

                isOrderedListLine(line) -> {
                    val (items, nextIndex) = parseOrderedList(lines, i)
                    elements.add(MarkdownElement.OrderedList("", items))
                    i = nextIndex
                }

                else -> {
                    // 段落
                    val (paragraph, nextIndex) = parseParagraph(lines, i)
                    elements.add(paragraph)
                    i = nextIndex
                }
            }
        }

        return elements
    }
    
    override fun parseInline(text: String): List<MarkdownToken> {
        val tokens = mutableListOf<MarkdownToken>()
        
        // 使用单个合并正则表达式一次性扫描所有 Token
        // 优先级：删除线、高亮、粗体、斜体、链接、下划线、标签
        val combinedPattern = Regex(
            "(~~([^~]+)~~)|" +                    // 1-2: 删除线
            "(==([^=]+)==)|" +                   // 3-4: 高亮
            "(\\*\\*([^*]+)\\*\\*)|" +            // 5-6: 粗体 **
            "(__([^_]+)__)|" +                   // 7-8: 粗体 __
            "(\\*([^*]+)\\*)|" +                 // 9-10: 斜体 *
            "(_([^_]+)_)|" +                     // 11-12: 斜体 _
            "(\\[([^]]+)]\\(([^)]+)\\))|" +    // 13-15: 链接 [text](url)
            "(<u>([^<]+)</u>)|" +                 // 16-17: 下划线
            "(#([\\w\\u4e00-\\u9fa5]+))"         // 18-19: 标签
        )
        
        var lastEnd = 0
        
        combinedPattern.findAll(text).forEach { match ->
            // 添加匹配前的普通文本
            if (match.range.first > lastEnd) {
                tokens.add(MarkdownToken.Text(text.substring(lastEnd, match.range.first)))
            }
            
            // 根据捕获组判断 Token 类型
            when {
                match.groups[1] != null -> { // 删除线 ~~
                    tokens.add(MarkdownToken.Strikethrough(match.groups[2]!!.value))
                }
                match.groups[3] != null -> { // 高亮 ==
                    tokens.add(MarkdownToken.Highlight(match.groups[4]!!.value))
                }
                match.groups[5] != null -> { // 粗体 **
                    tokens.add(MarkdownToken.Bold(match.groups[6]!!.value))
                }
                match.groups[7] != null -> { // 粗体 __
                    tokens.add(MarkdownToken.Bold(match.groups[8]!!.value))
                }
                match.groups[9] != null -> { // 斜体 *
                    tokens.add(MarkdownToken.Italic(match.groups[10]!!.value))
                }
                match.groups[11] != null -> { // 斜体 _
                    tokens.add(MarkdownToken.Italic(match.groups[12]!!.value))
                }
                match.groups[13] != null -> { // 链接 [text](url)
                    tokens.add(MarkdownToken.Link(match.groups[14]!!.value, match.groups[15]!!.value))
                }
                match.groups[16] != null -> { // 下划线 <u>
                    tokens.add(MarkdownToken.Underline(match.groups[17]!!.value))
                }
                match.groups[18] != null -> { // 标签 #
                    tokens.add(MarkdownToken.Tag(match.groups[19]!!.value))
                }
            }
            
            lastEnd = match.range.last + 1
        }
        
        // 添加剩余文本
        if (lastEnd < text.length) {
            tokens.add(MarkdownToken.Text(text.substring(lastEnd)))
        }
        
        return tokens
    }
    
    private fun isUnorderedListLine(line: String): Boolean {
        return line.matches(Regex("^\\s*[-*+]\\s+.*"))
    }
    
    private fun isOrderedListLine(line: String): Boolean {
        // 确保不是标题（不以 # 开头）
        if (line.trim().startsWith("#")) return false
        return line.matches(Regex("^\\s*\\d+\\.\\s+.*"))
    }
    
    private fun parseUnorderedList(lines: List<String>, startIndex: Int): Pair<List<ListItem>, Int> {
        val items = mutableListOf<ListItem>()
        var i = startIndex
        val baseIndent = countIndent(lines[startIndex])

        while (i < lines.size) {
            val line = lines[i]

            // 关键修复：如果遇到标题行，立即结束列表解析
            if (isHeaderLine(line)) break

            if (line.isBlank()) {
                i++
                continue
            }

            val currentIndent = countIndent(line)
            if (currentIndent < baseIndent) break

            if (isUnorderedListLine(line)) {
                val content = extractListContent(line)
                // 使用相对缩进级别，而不是固定除以2
                val indentLevel = if (currentIndent > baseIndent) 1 else 0
                items.add(ListItem(content, indentLevel))
                i++
            } else if (isOrderedListLine(line)) {
                break
            } else {
                // 继续上一项的内容（使用换行符保留换行）
                if (items.isNotEmpty()) {
                    val lastItem = items.last()
                    items[items.size - 1] = lastItem.copy(
                        content = lastItem.content + "\n" + line.trim()
                    )
                }
                i++
            }
        }

        return items to i
    }

    private fun parseOrderedList(lines: List<String>, startIndex: Int): Pair<List<Pair<Int, ListItem>>, Int> {
        val items = mutableListOf<Pair<Int, ListItem>>()
        var i = startIndex
        val baseIndent = countIndent(lines[startIndex])
        var currentNumber = 1

        while (i < lines.size) {
            val line = lines[i]

            // 关键修复：如果遇到标题行，立即结束列表解析
            if (isHeaderLine(line)) break

            if (line.isBlank()) {
                i++
                continue
            }

            val currentIndent = countIndent(line)
            if (currentIndent < baseIndent) break

            if (isOrderedListLine(line)) {
                val content = extractOrderedListContent(line)
                // 使用相对缩进级别，而不是固定除以2
                val indentLevel = if (currentIndent > baseIndent) 1 else 0
                items.add(currentNumber to ListItem(content, indentLevel))
                currentNumber++
                i++
            } else if (isUnorderedListLine(line)) {
                break
            } else {
                // 继续上一项的内容（使用换行符保留换行）
                if (items.isNotEmpty()) {
                    val (num, lastItem) = items.last()
                    items[items.size - 1] = num to lastItem.copy(
                        content = lastItem.content + "\n" + line.trim()
                    )
                }
                i++
            }
        }

        return items to i
    }
    
    private fun parseParagraph(lines: List<String>, startIndex: Int): Pair<MarkdownElement.Paragraph, Int> {
        val content = StringBuilder()
        var i = startIndex
        
        while (i < lines.size) {
            val line = lines[i]
            // 修复：添加标题判断，防止标题被吞入段落
            if (line.isBlank() || isUnorderedListLine(line) || isOrderedListLine(line) || isHeaderLine(line)) {
                break
            }
            
            if (content.isNotEmpty()) {
                content.append("\n")
            }
            content.append(line.trim())
            i++
        }
        
        val rawText = lines.subList(startIndex, i).joinToString("\n")
        return MarkdownElement.Paragraph(rawText, content.toString()) to i
    }
    
    private fun countIndent(line: String): Int {
        return line.takeWhile { it.isWhitespace() }.length
    }
    
    private fun extractListContent(line: String): String {
        val match = Regex("^\\s*[-*+]\\s+(.*)$").find(line)
        return match?.groupValues?.get(1) ?: line.trim()
    }
    
    private fun extractOrderedListContent(line: String): String {
        val match = Regex("^\\s*\\d+\\.\\s+(.*)$").find(line)
        return match?.groupValues?.get(1) ?: line.trim()
    }

    // 标题相关函数
    private fun isHeaderLine(line: String): Boolean {
        return Regex("^#{1,6}\\s+.+$").matches(line.trim())
    }

    private fun parseHeader(lines: List<String>, startIndex: Int): Pair<MarkdownElement.Header, Int> {
        val line = lines[startIndex].trim()
        val match = Regex("^(#{1,6})\\s+(.+)$").find(line)
        val level = match?.groupValues?.get(1)?.length ?: 1
        val content = match?.groupValues?.get(2) ?: line
        return MarkdownElement.Header(line, level, content) to (startIndex + 1)
    }
}
