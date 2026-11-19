package com.example.services

/**
 * Channel selection for the focus/break completion alert.
 *
 * Android 8+ locks a notification channel's sound and vibration at creation
 * time, so a single per-notification `.setSound()` cannot turn them on. The
 * app therefore owns four channels and picks the one matching the user's
 * settings. Kept Android-free so it can be unit tested.
 */
object FocusCompletionChannel {

    const val SOUND_AND_VIBRATION = "focus_completion_v3_sound_vibrate"
    const val SOUND_ONLY = "focus_completion_v3_sound"
    const val VIBRATION_ONLY = "focus_completion_v3_vibrate"
    const val SILENT = "focus_completion_v3_silent"

    fun idFor(sound: Boolean, vibrate: Boolean): String = when {
        sound && vibrate -> SOUND_AND_VIBRATION
        sound -> SOUND_ONLY
        vibrate -> VIBRATION_ONLY
        else -> SILENT
    }

    fun nameFor(sound: Boolean, vibrate: Boolean): String = when {
        sound && vibrate -> "Session Completion (Sound & Vibration)"
        sound -> "Session Completion (Sound)"
        vibrate -> "Session Completion (Vibration)"
        else -> "Session Completion (Silent)"
    }

    /**
     * Sound is used only when voice is not the alert, so the spoken message is
     * not drowned by the alarm ringtone.
     */
    fun useSound(completionSoundEnabled: Boolean, voiceSpoken: Boolean): Boolean =
        completionSoundEnabled && !voiceSpoken
}
