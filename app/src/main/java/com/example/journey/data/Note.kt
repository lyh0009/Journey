package com.example.journey.data

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class Note(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val tags: List<String> = emptyList(),
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    val createdAt: LocalDateTime
        get() = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(createdAtMillis),
            ZoneId.systemDefault()
        )

    val formattedDate: String
        get() = createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
}
