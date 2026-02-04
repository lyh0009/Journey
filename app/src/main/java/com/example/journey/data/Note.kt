package com.example.journey.data

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Note(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val tags: List<String> = emptyList(),
    val createdAt: LocalDateTime? = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null,
) {
    val formattedDate: String
        get() = createdAt?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            ?: LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

    val formattedUpdatedDate: String
        get() = updatedAt?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            ?: formattedDate
}
