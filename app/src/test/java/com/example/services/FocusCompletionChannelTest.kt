package com.example.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusCompletionChannelTest {

    @Test
    fun everyCombinationHasItsOwnChannel() {
        val ids = listOf(
            FocusCompletionChannel.idFor(sound = true, vibrate = true),
            FocusCompletionChannel.idFor(sound = true, vibrate = false),
            FocusCompletionChannel.idFor(sound = false, vibrate = true),
            FocusCompletionChannel.idFor(sound = false, vibrate = false)
        )
        assertEquals(4, ids.toSet().size)
    }

    @Test
    fun silentCombinationUsesSilentChannel() {
        assertEquals(
            FocusCompletionChannel.SILENT,
            FocusCompletionChannel.idFor(sound = false, vibrate = false)
        )
    }

    @Test
    fun volumeChannelMatchesSettings() {
        assertEquals(
            FocusCompletionChannel.SOUND_ONLY,
            FocusCompletionChannel.idFor(sound = true, vibrate = false)
        )
        assertEquals(
            FocusCompletionChannel.VIBRATION_ONLY,
            FocusCompletionChannel.idFor(sound = false, vibrate = true)
        )
        assertEquals(
            FocusCompletionChannel.SOUND_AND_VIBRATION,
            FocusCompletionChannel.idFor(sound = true, vibrate = true)
        )
    }

    @Test
    fun voiceSuppressesAlarmSound() {
        // Voice is the alert; playing the alarm too would drown it out.
        assertFalse(FocusCompletionChannel.useSound(completionSoundEnabled = true, voiceSpoken = true))
        assertTrue(FocusCompletionChannel.useSound(completionSoundEnabled = true, voiceSpoken = false))
    }

    @Test
    fun soundSettingWinsWhenVoiceIsOff() {
        assertFalse(FocusCompletionChannel.useSound(completionSoundEnabled = false, voiceSpoken = false))
        assertFalse(FocusCompletionChannel.useSound(completionSoundEnabled = false, voiceSpoken = true))
    }

    @Test
    fun channelNamesAreReadable() {
        assertEquals(
            "Session Completion (Sound & Vibration)",
            FocusCompletionChannel.nameFor(sound = true, vibrate = true)
        )
        assertEquals(
            "Session Completion (Silent)",
            FocusCompletionChannel.nameFor(sound = false, vibrate = false)
        )
    }
}
