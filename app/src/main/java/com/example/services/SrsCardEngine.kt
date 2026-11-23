package com.example.services

import com.example.data.Flashcard
import com.example.data.SrsRating
import kotlin.math.roundToInt

object SrsCardEngine {
    const val MIN_EASE_FACTOR: Float = 1.3f
    const val MAX_EASE_FACTOR: Float = 3.5f
    const val DEFAULT_EASE_FACTOR: Float = 2.5f

    /**
     * Calculates updated flashcard SRS fields based on user rating.
     * Uses the SuperMemo SM-2 algorithm adapted for mobile spaced repetition.
     */
    fun calculateNextReview(
        card: Flashcard,
        rating: SrsRating,
        nowMs: Long = System.currentTimeMillis()
    ): Flashcard {
        var newRepetition = card.repetitionCount
        var newLapses = card.lapses
        var newEase = card.easeFactor
        val newInterval: Int

        when (rating) {
            SrsRating.AGAIN -> {
                newRepetition = 0
                newLapses += 1
                newInterval = 1
                newEase = (newEase - 0.2f).coerceAtLeast(MIN_EASE_FACTOR)
            }
            SrsRating.HARD -> {
                newInterval = if (newRepetition == 0) {
                    1
                } else {
                    (card.intervalDays * 1.2f).roundToInt().coerceAtLeast(1)
                }
                newEase = (newEase - 0.15f).coerceAtLeast(MIN_EASE_FACTOR)
                newRepetition += 1
            }
            SrsRating.GOOD -> {
                newInterval = when (newRepetition) {
                    0 -> 1
                    1 -> 6
                    else -> (card.intervalDays * newEase).roundToInt().coerceAtLeast(card.intervalDays + 1)
                }
                newRepetition += 1
            }
            SrsRating.EASY -> {
                newInterval = when (newRepetition) {
                    0 -> 3
                    1 -> 8
                    else -> (card.intervalDays * newEase * 1.3f).roundToInt().coerceAtLeast(card.intervalDays + 2)
                }
                newEase = (newEase + 0.15f).coerceAtMost(MAX_EASE_FACTOR)
                newRepetition += 1
            }
        }

        val nextReviewMs = nowMs + (newInterval.toLong() * 24L * 60L * 60L * 1000L)

        return card.copy(
            intervalDays = newInterval,
            easeFactor = newEase,
            repetitionCount = newRepetition,
            lapses = newLapses,
            nextReviewDateMs = nextReviewMs,
            lastReviewedMs = nowMs
        )
    }

    /**
     * Predicts the next interval in days for a given rating without mutating card.
     */
    fun predictInterval(card: Flashcard, rating: SrsRating): Int {
        return calculateNextReview(card, rating).intervalDays
    }

    /**
     * Formats an interval into user-friendly text, e.g. "1d", "6d", "1.5mo"
     */
    fun formatInterval(days: Int): String {
        return when {
            days <= 0 -> "<1d"
            days == 1 -> "1d"
            days < 30 -> "${days}d"
            days < 365 -> {
                val months = days / 30.0
                if (months % 1.0 < 0.2) "${months.toInt()}mo" else String.format(java.util.Locale.US, "%.1fmo", months)
            }
            else -> {
                val years = days / 365.0
                String.format(java.util.Locale.US, "%.1fyr", years)
            }
        }
    }

    /**
     * Determines mastery stage of a card.
     */
    fun getMasteryStage(card: Flashcard): String {
        return when {
            card.repetitionCount == 0 -> "New"
            card.intervalDays < 7 -> "Learning"
            card.intervalDays < 21 -> "Review"
            else -> "Mastered"
        }
    }

    /**
     * Checks if card is due for review.
     */
    fun isDue(card: Flashcard, nowMs: Long = System.currentTimeMillis()): Boolean {
        return card.nextReviewDateMs <= nowMs
    }
}
