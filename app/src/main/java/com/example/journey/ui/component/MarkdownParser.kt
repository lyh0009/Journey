package com.example.journey.ui.component

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

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
    fun listMarker(color: androidx.compose.ui.graphics.Color) = 
        SpanStyle(color = color, fontWeight = FontWeight.Bold)
}

/**
 * Markdown 解析器实现
 * 支持：段落、无序列表、有序列表、加粗、斜体、删除线、高亮、下划线、链接
 * 不支持：标题、行内代码、引用块、分隔线、表格
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
                    i++
                    continue
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
        var remaining = text
        
        // 按优先级定义正则表达式
        val patterns = listOf(
            Regex("~~([^~]+)~~") to { match: MatchResult ->
                MarkdownToken.Strikethrough(match.groupValues[1])
            },
            Regex("==([^=]+)==") to { match: MatchResult ->
                MarkdownToken.Highlight(match.groupValues[1])
            },
            Regex("\\*\\*([^*]+)\\*\\*") to { match: MatchResult ->
                MarkdownToken.Bold(match.groupValues[1])
            },
            Regex("__([^_]+)__") to { match: MatchResult ->
                MarkdownToken.Bold(match.groupValues[1])
            },
            Regex("(?<!\\*)\\*([^*]+)\\*(?!\\*)") to { match: MatchResult ->
                MarkdownToken.Italic(match.groupValues[1])
            },
            Regex("(?<!_)_([^_]+)_(?!_)") to { match: MatchResult ->
                MarkdownToken.Italic(match.groupValues[1])
            },
            Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)") to { match: MatchResult ->
                MarkdownToken.Link(match.groupValues[1], match.groupValues[2])
            },
            Regex("<u>([^<]+)</u>") to { match: MatchResult ->
                MarkdownToken.Underline(match.groupValues[1])
            }
        )
        
        while (remaining.isNotEmpty()) {
            var earliestMatch: Pair<MatchResult, (MatchResult) -> MarkdownToken>? = null
            var earliestStart = Int.MAX_VALUE
            
            for ((regex, factory) in patterns) {
                val match = regex.find(remaining)
                if (match != null && match.range.first < earliestStart) {
                    earliestStart = match.range.first
                    earliestMatch = match to factory
                }
            }
            
            if (earliestMatch == null) {
                tokens.add(MarkdownToken.Text(remaining))
                break
            } else {
                val (match, factory) = earliestMatch
                
                if (match.range.first > 0) {
                    tokens.add(MarkdownToken.Text(remaining.substring(0, match.range.first)))
                }
                
                tokens.add(factory(match))
                remaining = remaining.substring(match.range.last + 1)
            }
        }
        
        return tokens
    }
    
    private fun isUnorderedListLine(line: String): Boolean {
        return line.matches(Regex("^\\s*[-*+]\\s+.*"))
    }
    
    private fun isOrderedListLine(line: String): Boolean {
        return line.matches(Regex("^\\s*\\d+\\.\\s+.*"))
    }
    
    private fun parseUnorderedList(lines: List<String>, startIndex: Int): Pair<List<ListItem>, Int> {
        val items = mutableListOf<ListItem>()
        var i = startIndex
        val baseIndent = countIndent(lines[startIndex])
        
        while (i < lines.size) {
            val line = lines[i]
            if (line.isBlank()) {
                i++
                continue
            }
            
            val currentIndent = countIndent(line)
            if (currentIndent < baseIndent) break
            
            if (isUnorderedListLine(line)) {
                val content = extractListContent(line)
                val indentLevel = (currentIndent - baseIndent) / 2
                items.add(ListItem(content, indentLevel))
                i++
            } else if (isOrderedListLine(line)) {
                break
            } else {
                // 继续上一项的内容
                if (items.isNotEmpty()) {
                    val lastItem = items.last()
                    items[items.size - 1] = lastItem.copy(
                        content = lastItem.content + " " + line.trim()
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
            if (line.isBlank()) {
                i++
                continue
            }
            
            val currentIndent = countIndent(line)
            if (currentIndent < baseIndent) break
            
            if (isOrderedListLine(line)) {
                val content = extractOrderedListContent(line)
                val indentLevel = (currentIndent - baseIndent) / 2
                items.add(currentNumber to ListItem(content, indentLevel))
                currentNumber++
                i++
            } else if (isUnorderedListLine(line)) {
                break
            } else {
                if (items.isNotEmpty()) {
                    val (num, lastItem) = items.last()
                    items[items.size - 1] = num to lastItem.copy(
                        content = lastItem.content + " " + line.trim()
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
            if (line.isBlank() || isUnorderedListLine(line) || isOrderedListLine(line)) {
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
}
