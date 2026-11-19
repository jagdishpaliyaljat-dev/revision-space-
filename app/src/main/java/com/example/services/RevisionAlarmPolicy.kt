package com.example.services

/**
 * Pure decision helpers for the per-topic revision alarms.
 *
 * Kept free of Android APIs so the rules can be unit tested on the JVM.
 */
object RevisionAlarmPolicy {

    /** Alarms are matched to the revision date at scheduling time. */
    private const val FORWARD_TOLERANCE_MS = 60_000L

    /**
     * True when a delivered alarm no longer matches the topic's latest revision
     * date:
     *
     * - the topic has no pending revision left (date 0), or
     * - a revision was completed and the date moved forward, but the old alarm
     *   could not be cancelled in time.
     *
     * A date that is *older* than the alarm is not stale: it means the revision
     * is overdue or the user snoozed the notification.
     */
    fun isStale(topicNextRevisionMs: Long, alarmNextRevisionMs: Long): Boolean {
        if (topicNextRevisionMs == 0L) return true
        if (alarmNextRevisionMs <= 0L) return false
        return topicNextRevisionMs > alarmNextRevisionMs + FORWARD_TOLERANCE_MS
    }
}
