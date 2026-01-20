package com.example.journey.data

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Note(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    val formattedDate: String
        get() = createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
}