## 问题分析

当前 NoteCard 中超过5行的文本没有正常截断，也没有显示展开按钮。

**根本原因：**
1. MarkdownRenderer 的 maxLines 只限制元素数量，不限制实际渲染行数
2. 溢出检测使用隐藏的 Text 组件，与 MarkdownRenderer 渲染逻辑不一致

## 修复方案

### 文件1: MarkdownRenderer.kt
- 给所有 Text 组件添加 `overflow = TextOverflow.Ellipsis`
- 添加 `onTextLayout` 回调参数，用于检测溢出

### 文件2: NoteCard.kt  
- 移除隐藏的 Text 检测逻辑
- 使用 MarkdownRenderer 的 onTextLayout 回调检测溢出
- 简化溢出判断逻辑

## 修改内容预览

**MarkdownRenderer.kt:**
- 添加 `onTextLayout: (TextLayoutResult) -> Unit` 参数
- 所有 Text 组件添加 `overflow = TextOverflow.Ellipsis`
- ParagraphElement 传递 onTextLayout 回调

**NoteCard.kt:**
- 删除隐藏的 Text 组件（约15行代码）
- 通过 MarkdownRenderer 的 onTextLayout 获取实际行数
- 修复 isOverFlowed 判断逻辑