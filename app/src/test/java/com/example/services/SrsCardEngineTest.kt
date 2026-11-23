package com.example.services

import com.example.data.Flashcard
import com.example.data.SrsRating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SrsCardEngineTest {

    @Test
    fun newCardDefaults() {
        val card = Flashcard(id = "card1", front = "Question", back = "Answer")
        assertEquals(0, card.repetitionCount)
        assertEquals(1, card.intervalDays)
        assertEquals(2.5f, card.easeFactor, 0.001f)
        assertEquals("New", SrsCardEngine.getMasteryStage(card))
    }

    @Test
    fun reviewAgain_resetsRepetitionsAndIncreasesLapses() {
        val card = Flashcard(
            id = "card1",
            repetitionCount = 3,
            intervalDays = 15,
            easeFactor = 2.5f,
            lapses = 0
        )
        val now = 1_700_000_000_000L
        val updated = SrsCardEngine.calculateNextReview(card, SrsRating.AGAIN, now)

        assertEquals(0, updated.repetitionCount)
        assertEquals(1, updated.lapses)
        assertEquals(1, updated.intervalDays)
        assertEquals(2.3f, updated.easeFactor, 0.001f)
        assertEquals(now + 1 * 24L * 60L * 60L * 1000L, updated.nextReviewDateMs)
        assertEquals("New", SrsCardEngine.getMasteryStage(updated))
    }

    @Test
    fun reviewHard_incrementsRepetitionWithSlightEasePenalty() {
        val card = Flashcard(
            id = "card1",
            repetitionCount = 2,
            intervalDays = 6,
            easeFactor = 2.5f
        )
        val updated = SrsCardEngine.calculateNextReview(card, SrsRating.HARD)

        assertEquals(3, updated.repetitionCount)
        assertEquals(2.35f, updated.easeFactor, 0.001f)
        // 6 * 1.2 = 7.2 -> 7 days
        assertEquals(7, updated.intervalDays)
    }

    @Test
    fun reviewGood_progressesIntervalsProperly() {
        var card = Flashcard(id = "card1", repetitionCount = 0, intervalDays = 1, easeFactor = 2.5f)

        // First Good review -> interval 1
        card = SrsCardEngine.calculateNextReview(card, SrsRating.GOOD)
        assertEquals(1, card.repetitionCount)
        assertEquals(1, card.intervalDays)
        assertEquals(2.5f, card.easeFactor, 0.001f)

        // Second Good review -> interval 6
        card = SrsCardEngine.calculateNextReview(card, SrsRating.GOOD)
        assertEquals(2, card.repetitionCount)
        assertEquals(6, card.intervalDays)

        // Third Good review -> interval 6 * 2.5 = 15
        card = SrsCardEngine.calculateNextReview(card, SrsRating.GOOD)
        assertEquals(3, card.repetitionCount)
        assertEquals(15, card.intervalDays)
    }

    @Test
    fun reviewEasy_bonusIntervalAndIncreasesEase() {
        val card = Flashcard(id = "card1", repetitionCount = 1, intervalDays = 6, easeFactor = 2.5f)
        val updated = SrsCardEngine.calculateNextReview(card, SrsRating.EASY)

        assertEquals(2, updated.repetitionCount)
        assertEquals(2.65f, updated.easeFactor, 0.001f)
        assertEquals(8, updated.intervalDays)
    }

    @Test
    fun easeFactorNeverDropsBelowMinimum() {
        var card = Flashcard(id = "card1", easeFactor = 1.4f)
        card = SrsCardEngine.calculateNextReview(card, SrsRating.AGAIN)
        // 1.4 - 0.2 = 1.2, but should clamp to MIN_EASE_FACTOR = 1.3
        assertEquals(SrsCardEngine.MIN_EASE_FACTOR, card.easeFactor, 0.001f)

        // Review again remains at 1.3
        card = SrsCardEngine.calculateNextReview(card, SrsRating.AGAIN)
        assertEquals(SrsCardEngine.MIN_EASE_FACTOR, card.easeFactor, 0.001f)
    }

    @Test
    fun predictIntervalMatchesActual() {
        val card = Flashcard(id = "card1", repetitionCount = 2, intervalDays = 10, easeFactor = 2.5f)
        for (rating in SrsRating.values()) {
            val predicted = SrsCardEngine.predictInterval(card, rating)
            val actual = SrsCardEngine.calculateNextReview(card, rating).intervalDays
            assertEquals(predicted, actual)
        }
    }

    @Test
    fun formatIntervalOutputs() {
        assertEquals("<1d", SrsCardEngine.formatInterval(0))
        assertEquals("1d", SrsCardEngine.formatInterval(1))
        assertEquals("6d", SrsCardEngine.formatInterval(6))
        assertEquals("20d", SrsCardEngine.formatInterval(20))
        assertEquals("2mo", SrsCardEngine.formatInterval(60))
    }

    @Test
    fun isDueChecks() {
        val now = 1_700_000_000_000L
        val dueCard = Flashcard(nextReviewDateMs = now - 1000L)
        val futureCard = Flashcard(nextReviewDateMs = now + 1000L)

        assertTrue(SrsCardEngine.isDue(dueCard, now))
        assertFalse(SrsCardEngine.isDue(futureCard, now))
    }
}
