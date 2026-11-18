package com.example.data
import com.example.ui.*


data class ManualRevisionPayload(
    val topicId: String,
    val dateMs: Long,
    val durationMins: Int,
    val revisionNumber: Int,
    val notes: String,
    val status: String
)
