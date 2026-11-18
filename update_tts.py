import re

with open("app/src/main/java/com/example/services/FocusTimerService.kt", "r") as f:
    content = f.read()

content = content.replace('''            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(java.util.Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    android.util.Log.e("FocusTimerService", "TTS Language US not supported")
                } else {
                    isTtsInitialized = true
                }
            }''', '''            if (status == TextToSpeech.SUCCESS) {
                val currentLocale = java.util.Locale.getDefault()
                val result = tts?.setLanguage(currentLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to English
                    val fallbackResult = tts?.setLanguage(java.util.Locale.ENGLISH)
                    if (fallbackResult != TextToSpeech.LANG_MISSING_DATA && fallbackResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                        isTtsInitialized = true
                    }
                } else {
                    isTtsInitialized = true
                }
            }''')

with open("app/src/main/java/com/example/services/FocusTimerService.kt", "w") as f:
    f.write(content)
