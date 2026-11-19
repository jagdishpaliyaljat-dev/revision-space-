package com.example.services

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RevisionAlarmPolicyTest {

    @Test
    fun alarmMatchingTopicIsNotStale() {
        val date = 1_700_000_000_000L
        assertFalse(RevisionAlarmPolicy.isStale(date, date))
    }

    @Test
    fun alarmWithinToleranceIsNotStale() {
        val date = 1_700_000_000_000L
        assertFalse(RevisionAlarmPolicy.isStale(date + 30_000L, date))
    }

    @Test
    fun alarmIsStaleWhenRevisionMovedForward() {
        val date = 1_700_000_000_000L
        assertTrue(RevisionAlarmPolicy.isStale(date + 86_400_000L, date))
    }

    @Test
    fun overdueRevisionIsNotStale() {
        val date = 1_700_000_000_000L
        assertFalse(RevisionAlarmPolicy.isStale(date, date + 86_400_000L))
    }

    @Test
    fun snoozedAlarmIsNotStale() {
        // Snooze schedules a new alarm while the topic date stays in the past.
        val topicDate = 1_700_000_000_000L
        val snoozeDate = topicDate + 30 * 60 * 1000L
        assertFalse(RevisionAlarmPolicy.isStale(topicDate, snoozeDate))
    }

    @Test
    fun topicWithoutPendingRevisionIsStale() {
        assertTrue(RevisionAlarmPolicy.isStale(0L, 1_700_000_000_000L))
    }

    @Test
    fun missingAlarmDateIsTreatedAsFresh() {
        assertFalse(RevisionAlarmPolicy.isStale(1_700_000_000_000L, 0L))
    }
}
