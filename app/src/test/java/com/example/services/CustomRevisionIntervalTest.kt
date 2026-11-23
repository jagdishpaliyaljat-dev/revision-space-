package com.example.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomRevisionIntervalTest {

    @Test
    fun presetsAreValidAndStrictlyIncreasing() {
        for ((name, pattern) in RevisionEngine.PRESETS) {
            val (isValid, msg) = RevisionEngine.validatePatternInput(pattern.joinToString(", "))
            assertTrue("Preset '$name' should be valid, got: $msg", isValid)
            assertTrue("Preset '$name' must not be empty", pattern.isNotEmpty())
            for (i in 0 until pattern.size - 1) {
                assertTrue("Intervals must strictly increase", pattern[i] < pattern[i + 1])
            }
        }
    }

    @Test
    fun validatePatternInput_validCases() {
        val (ok1, _) = RevisionEngine.validatePatternInput("1, 3, 7, 14, 30")
        assertTrue(ok1)

        val (ok2, _) = RevisionEngine.validatePatternInput("1,2,4,7,14")
        assertTrue(ok2)
    }

    @Test
    fun validatePatternInput_invalidCases() {
        // Blank
        val (b1, _) = RevisionEngine.validatePatternInput("")
        assertFalse(b1)

        // Trailing/leading empty
        val (b2, _) = RevisionEngine.validatePatternInput("1, 3, , 7")
        assertFalse(b2)

        // Decimals
        val (b3, _) = RevisionEngine.validatePatternInput("1, 2.5, 7")
        assertFalse(b3)

        // Negative or Zero
        val (b4, _) = RevisionEngine.validatePatternInput("0, 3, 7")
        assertFalse(b4)

        // Duplicates
        val (b5, _) = RevisionEngine.validatePatternInput("1, 3, 3, 7")
        assertFalse(b5)

        // Decreasing / Non-increasing
        val (b6, _) = RevisionEngine.validatePatternInput("1, 7, 3")
        assertFalse(b6)
    }

    @Test
    fun schedulePreviewGeneratesCorrectPairs() {
        val pattern = listOf(1, 3, 7)
        val startMs = 1_700_000_000_000L
        val preview = RevisionEngine.calculateSchedulePreview(pattern, startMs)

        assertEquals(3, preview.size)
        assertEquals(1, preview[0].first)
        assertEquals(3, preview[1].first)
        assertEquals(7, preview[2].first)
        assertTrue(preview[0].second.isNotBlank())
    }
}
