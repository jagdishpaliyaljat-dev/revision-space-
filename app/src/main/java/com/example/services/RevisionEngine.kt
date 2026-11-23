package com.example.services

import java.util.Calendar

object RevisionEngine {
    val EASY_PATTERN: List<Int> = listOf(1, 4, 10, 20, 30)
    val MEDIUM_PATTERN: List<Int> = listOf(1, 3, 7, 14, 21, 30)
    val HARD_PATTERN: List<Int> = listOf(1, 2, 4, 7, 14, 21, 28)

    val PRESET_STANDARD: List<Int> = MEDIUM_PATTERN
    val PRESET_INTENSIVE: List<Int> = listOf(1, 2, 4, 7, 14)
    val PRESET_LONG_TERM: List<Int> = listOf(1, 7, 16, 35, 90)
    val PRESET_LEITNER: List<Int> = listOf(1, 3, 5, 10, 20)

    val PRESETS = mapOf(
        "Standard (1, 3, 7, 14, 21, 30)" to PRESET_STANDARD,
        "Intensive (1, 2, 4, 7, 14)" to PRESET_INTENSIVE,
        "Long-Term (1, 7, 16, 35, 90)" to PRESET_LONG_TERM,
        "Leitner (1, 3, 5, 10, 20)" to PRESET_LEITNER
    )

    val DEFAULT_PATTERN: List<Int> = MEDIUM_PATTERN
    const val RECURRING_INTERVAL_DAYS: Int = 30

    fun getPatternForDifficulty(difficulty: String): List<Int> {
        return when (difficulty.trim().lowercase(java.util.Locale.getDefault())) {
            "easy" -> EASY_PATTERN
            "hard" -> HARD_PATTERN
            else -> MEDIUM_PATTERN
        }
    }

    /**
     * Calculates the next revision timestamp in milliseconds.
     * Easy: Day 1 -> Day 4 -> Day 10 -> Day 20 -> Day 30 -> Every 30 days
     * Medium: Day 1 -> Day 3 -> Day 7 -> Day 14 -> Day 21 -> Day 30 -> Every 30 days
     * Hard: Day 1 -> Day 2 -> Day 4 -> Day 7 -> Day 14 -> Day 21 -> Day 28 -> Every 30 days
     *
     * @param revisionCount The count of completed revisions (0 after first study).
     * @param pattern The initial interval days list.
     * @param firstStudyMs Timestamp of the initial study session.
     * @param lastRevisionMs Timestamp of the most recent revision session.
     */
    fun calculateNextRevisionTimestamp(
        revisionCount: Int,
        pattern: List<Int> = DEFAULT_PATTERN,
        firstStudyMs: Long,
        lastRevisionMs: Long = System.currentTimeMillis()
    ): Long {
        val activePattern = if (pattern.isNotEmpty()) pattern else DEFAULT_PATTERN
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        if (revisionCount <= 0) {
            val days = activePattern[0]
            val baseMs = if (firstStudyMs > 0L) firstStudyMs else now
            calendar.timeInMillis = baseMs
            calendar.add(Calendar.DAY_OF_YEAR, days)
            return calendar.timeInMillis
        } else if (revisionCount < activePattern.size) {
            val days = activePattern[revisionCount]
            val baseMs = if (firstStudyMs > 0L) firstStudyMs else (if (lastRevisionMs > 0L) lastRevisionMs else now)
            calendar.timeInMillis = baseMs
            calendar.add(Calendar.DAY_OF_YEAR, days)
            return calendar.timeInMillis
        } else {
            val baseMs = if (lastRevisionMs > 0L) lastRevisionMs else now
            calendar.timeInMillis = baseMs
            calendar.add(Calendar.DAY_OF_YEAR, RECURRING_INTERVAL_DAYS)
            return calendar.timeInMillis
        }
    }

    fun calculateRevisionTimestamps(pattern: List<Int>, startDateMs: Long): List<Long> {
        val activePattern = if (pattern.isNotEmpty()) pattern else DEFAULT_PATTERN
        val calendar = Calendar.getInstance()
        return activePattern.map { intervalDays ->
            calendar.timeInMillis = startDateMs
            calendar.add(Calendar.DAY_OF_YEAR, intervalDays)
            calendar.timeInMillis
        }
    }

    fun validatePatternInput(input: String): Pair<Boolean, String> {
        if (input.isBlank()) {
            return Pair(false, "Pattern cannot be empty")
        }
        val parts = input.split(",")
        val parsedList = mutableListOf<Int>()
        for (part in parts) {
            val trimmed = part.trim()
            if (trimmed.isEmpty()) {
                return Pair(false, "Empty interval found between commas")
            }
            val intValue = trimmed.toIntOrNull()
            if (intValue == null) {
                if (trimmed.contains(".")) {
                    return Pair(false, "Decimal values are not allowed: '$trimmed'")
                }
                return Pair(false, "Invalid number format: '$trimmed'")
            }
            if (intValue <= 0) {
                return Pair(false, "Intervals must be positive integers: $intValue")
            }
            if (intValue > 3650) {
                return Pair(false, "Interval value too large (max 3650 days): $intValue")
            }
            parsedList.add(intValue)
        }
        if (parsedList.distinct().size != parsedList.size) {
            return Pair(false, "Duplicate intervals are not allowed")
        }
        for (i in 0 until parsedList.size - 1) {
            if (parsedList[i] >= parsedList[i + 1]) {
                return Pair(false, "Intervals must be in strictly increasing order (e.g., 1, 3, 7)")
            }
        }
        return Pair(true, "")
    }

    /**
     * Returns a list of (intervalDays, formattedDate) for user-friendly preview.
     */
    fun calculateSchedulePreview(
        pattern: List<Int>,
        startDateMs: Long = System.currentTimeMillis()
    ): List<Pair<Int, String>> {
        val activePattern = if (pattern.isNotEmpty()) pattern else DEFAULT_PATTERN
        val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        val calendar = Calendar.getInstance()
        return activePattern.map { intervalDays ->
            calendar.timeInMillis = startDateMs
            calendar.add(Calendar.DAY_OF_YEAR, intervalDays)
            Pair(intervalDays, sdf.format(calendar.time))
        }
    }
}

