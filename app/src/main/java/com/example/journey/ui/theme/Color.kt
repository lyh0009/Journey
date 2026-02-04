package com.example.journey.ui.theme

import androidx.compose.ui.graphics.Color

// 基础颜色
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Markdown 主题颜色 - 浅色模式
object MarkdownLightColors {
    // 正文颜色
    val body = Color(0xFF212121)
    // 标题颜色
    val heading = Color(0xFF1A1A1A)
    // 引用块颜色
    val quote = Color(0xFF666666)
    // 代码颜色
    val code = Color(0xFFE53935)
    // 代码背景
    val codeBackground = Color(0xFFF5F5F5)
    // 链接颜色
    val link = Color(0xFF2196F3)
    // 高亮背景
    val highlight = Color(0xFFFFFF00)
    // 列表标记颜色
    val listMarker = Color(0xFF64B5F6)
    // 分隔线颜色
    val divider = Color(0xFFE0E0E0)
    // 提示文字颜色
    val hint = Color(0xFF9E9E9E)
    // 预览背景
    val previewBackground = Color(0xFFFAFAFA)
    // 表格边框
    val tableBorder = Color(0xFFE0E0E0)
    // 表格背景
    val tableBackground = Color(0xFFFFFFFF)
    // 表格交替行背景
    val tableAltBackground = Color(0xFFF5F5F5)
    // 引用块左边框
    val quoteBorder = Color(0xFF64B5F6)
    
    // 界面背景色
    val screenBackground = Color(0xFFF9F9F9)
    // 卡片背景色（白色）
    val cardBackground = Color(0xFFFFFFFF)
}

// Markdown 主题颜色 - 深色模式
object MarkdownDarkColors {
    // 正文颜色
    val body = Color(0xFFE0E0E0)
    // 标题颜色
    val heading = Color(0xFFFFFFFF)
    // 引用块颜色
    val quote = Color(0xFFB0B0B0)
    // 代码颜色
    val code = Color(0xFFFF8A80)
    // 代码背景
    val codeBackground = Color(0xFF2D2D2D)
    // 链接颜色
    val link = Color(0xFF64B5F6)
    // 高亮背景
    val highlight = Color(0xFF9E9E00)
    // 列表标记颜色
    val listMarker = Color(0xFF64B5F6)
    // 分隔线颜色
    val divider = Color(0xFF424242)
    // 提示文字颜色
    val hint = Color(0xFF757575)
    // 预览背景
    val previewBackground = Color(0xFF1E1E1E)
    // 表格边框
    val tableBorder = Color(0xFF424242)
    // 表格背景
    val tableBackground = Color(0xFF2D2D2D)
    // 表格交替行背景
    val tableAltBackground = Color(0xFF363636)
    // 引用块左边框
    val quoteBorder = Color(0xFF64B5F6)
    
    // 界面背景色（深色模式）
    val screenBackground = Color(0xFF121212)
    // 卡片背景色（深色模式）
    val cardBackground = Color(0xFF1E1E1E)
}

/**
 * Markdown 自定义颜色数据类
 */
data class MarkdownCustomColors(
    val markdownBody: Color,
    val markdownHeading: Color,
    val markdownQuote: Color,
    val markdownCode: Color,
    val markdownCodeText: Color,
    val markdownCodeBackground: Color,
    val markdownLink: Color,
    val markdownHighlight: Color,
    val markdownListMarker: Color,
    val markdownDivider: Color,
    val markdownHint: Color,
    val markdownPreviewBackground: Color,
    val markdownTableBorder: Color,
    val markdownTableBackground: Color,
    val markdownTableAltBackground: Color,
    val markdownQuoteBorder: Color,
    // 界面颜色
    val screenBackground: Color,
    val cardBackground: Color
)

/**
 * 获取浅色模式的 Markdown 颜色
 */
fun lightMarkdownCustomColors(): MarkdownCustomColors = MarkdownCustomColors(
    markdownBody = MarkdownLightColors.body,
    markdownHeading = MarkdownLightColors.heading,
    markdownQuote = MarkdownLightColors.quote,
    markdownCode = MarkdownLightColors.code,
    markdownCodeText = MarkdownLightColors.code,
    markdownCodeBackground = MarkdownLightColors.codeBackground,
    markdownLink = MarkdownLightColors.link,
    markdownHighlight = MarkdownLightColors.highlight,
    markdownListMarker = MarkdownLightColors.listMarker,
    markdownDivider = MarkdownLightColors.divider,
    markdownHint = MarkdownLightColors.hint,
    markdownPreviewBackground = MarkdownLightColors.previewBackground,
    markdownTableBorder = MarkdownLightColors.tableBorder,
    markdownTableBackground = MarkdownLightColors.tableBackground,
    markdownTableAltBackground = MarkdownLightColors.tableAltBackground,
    markdownQuoteBorder = MarkdownLightColors.quoteBorder,
    screenBackground = MarkdownLightColors.screenBackground,
    cardBackground = MarkdownLightColors.cardBackground
)

/**
 * 获取深色模式的 Markdown 颜色
 */
fun darkMarkdownCustomColors(): MarkdownCustomColors = MarkdownCustomColors(
    markdownBody = MarkdownDarkColors.body,
    markdownHeading = MarkdownDarkColors.heading,
    markdownQuote = MarkdownDarkColors.quote,
    markdownCode = MarkdownDarkColors.code,
    markdownCodeText = MarkdownDarkColors.code,
    markdownCodeBackground = MarkdownDarkColors.codeBackground,
    markdownLink = MarkdownDarkColors.link,
    markdownHighlight = MarkdownDarkColors.highlight,
    markdownListMarker = MarkdownDarkColors.listMarker,
    markdownDivider = MarkdownDarkColors.divider,
    markdownHint = MarkdownDarkColors.hint,
    markdownPreviewBackground = MarkdownDarkColors.previewBackground,
    markdownTableBorder = MarkdownDarkColors.tableBorder,
    markdownTableBackground = MarkdownDarkColors.tableBackground,
    markdownTableAltBackground = MarkdownDarkColors.tableAltBackground,
    markdownQuoteBorder = MarkdownDarkColors.quoteBorder,
    screenBackground = MarkdownDarkColors.screenBackground,
    cardBackground = MarkdownDarkColors.cardBackground
)
