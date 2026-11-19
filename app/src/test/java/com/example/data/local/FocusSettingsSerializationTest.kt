package com.example.data.local

import com.example.data.FocusSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusSettingsSerializationTest {

    @Test
    fun newAlertFlagsSurviveRoundTrip() {
        val original = FocusSettings(
            vibrationEnabled = false,
            completionSoundEnabled = false,
            voiceAnnouncementEnabled = false,
            ambientSound = "Brown Noise"
        )
        val decoded = decodeFocusSettings(encodeFocusSettings(original))

        assertFalse(decoded.vibrationEnabled)
        assertFalse(decoded.completionSoundEnabled)
        assertFalse(decoded.voiceAnnouncementEnabled)
        assertEquals("Brown Noise", decoded.ambientSound)
    }

    @Test
    fun oldSavedSettingsDefaultToAlertsOn() {
        // A value written before this feature existed must still decode.
        val old = "v1;fd=25;sbd=5;vib=true;snd=true;amb=None;dfg=120"
        val decoded = decodeFocusSettings(old)

        assertTrue(decoded.completionSoundEnabled)
        assertTrue(decoded.voiceAnnouncementEnabled)
        assertEquals(25, decoded.focusDuration)
    }

    @Test
    fun defaultsHaveVoiceAndSoundOn() {
        val defaults = decodeFocusSettings(null)
        assertTrue(defaults.completionSoundEnabled)
        assertTrue(defaults.voiceAnnouncementEnabled)
        assertTrue(defaults.vibrationEnabled)
    }
}
